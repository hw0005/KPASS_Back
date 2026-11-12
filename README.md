### 🛠️ 트러블슈팅 및 아키텍처 개선 (Troubleshooting) - 2026.10.27

#### 1. 서비스 로직(BO) 내 검증 코드 중복 및 단일 책임 원칙(SRP) 위배 개선
* **문제 상황**: `MemberBO` 내에 회원가입/로그인 비즈니스 로직과 DB 중복 검증 로직이 섞여 코드 가독성이 떨어지고 객체 책임이 비대해짐.
* **해결 방법**:
  * DB 검증 전담 객체인 `MemberValidator`를 별도로 분리하여 검증 책임을 이관.
  * `MemberBO`는 순수한 비즈니스 흐름 제어에만 집중하도록 리팩토링.
* **성과**: BO 객체의 단일 책임 원칙(SRP) 준수 및 검증 로직의 재사용성 확보.

#### 2. 예외 처리 일관성 확보 및 커스텀 에러 응답 표준화
* **문제 상황**: HTTP 400(Bad Request) 응답만으로는 '이메일 중복'과 '비밀번호 불일치' 등 프론트엔드가 세부 에러 원인을 구별할 수 없음.
* **해결 방법**:
  * `HttpStatus`, 커스텀 코드(`M001`~`M004`), 에러 메시지를 캡슐화한 `ErrorCode` Enum 구현.
  * `BusinessException`과 `GlobalExceptionHandler`를 통해 통일된 JSON 에러 구조 응답.
  * 흐름: 에러 발생 -> GLoablalExceptionHandler && Advice가 잡고-> BusinessException -> errorCode가져와서 저장 
* **성과**: 프론트엔드에서 `code`값(`M001`, `M004` 등)에 따라 특정 input 창 하단에 정확한 에러 메시지를 분기 출력 가능.

---

### 🛠️ 트러블슈팅 및 아키텍처 개선 (Troubleshooting) - 2026.10.28
#### 1. OAuth2 다중 프로바이더 확장성 확보 (전략 패턴 적용)
- **문제 상황:** Kakao, Naver, Google 등 소셜 제공자별 응답 구조와 토큰 교환 방식이 달라 코드 복잡도가 증가하고 확장성이 떨어짐.
- **해결 방법:**
  - `OAuthService` 인터페이스 기반의 전략 패턴(Strategy Pattern)을 적용.
  - 제공자별 개별 서비스 구현체를 작성하고, 반환 데이터는 `OAuthUserInfoDto`로 단일화.
- **성과:** Controller 수정 없이 새로운 OAuth Provider 추가가 가능한 유연하고 결합도 낮은 아키텍처 구축.

#### 2. 소셜 인증 및 2단계 회원가입(계좌번호 등록) UX 구현
- **문제 상황:** 소셜 로그인만으로 회원가입을 완결할 경우, K-PASS 환급 서비스에 필수적인 계좌번호 수집이 불가능함.
- **해결 방법:**
  - 백엔드 응답에 `isRegistered` 상태값 플래그 제공.
  - 프론트엔드 Callback에서 `isRegistered: false` 감지 시 계좌번호 입력 모달(2단계 가입 폼)을 띄워 회원가입을 완결하도록 연동.
- **성과:** 소셜 인증과 서비스 필수 정보 수집을 분리하여 매끄럽고 안전한 2단계 회원가입 흐름 완성.

#### 3. JPA 단건 조회 예외로 인한 500 에러 방지 및 중복 검증 개선
- **문제 상황:** `findByEmailOrNickname` 사용 시 이메일과 닉네임 중복 대상이 다를 경우 2건 이상이 조회되어 `NonUniqueResultException` 발생 (서버 500 에러).
- **해결 방법:** `MemberValidator` 내부 검증을 `existsByEmail` 및 `existsByNickname` 각각의 단독 조회 메서드로 분리.
- **성과:** 500 내부 서버 에러를 차단하고, 정의된 `BusinessException(DUPLICATE_EMAIL)`을 통해 프론트엔드에 명확한 예외 메시지(400/409) 전달.


---

### 🛠️ 트러블슈팅: KOPIS 외부 API 대용량 동기화 및 WAF(방화벽) 차단 극복 - 2026.11.02

#### 1. 문제 상황 (Issue)
- **현상**: 최근 5년 치 KOPIS 공연 데이터 전수 수집(약 5만~10만 건) 중, 수집 진행 중 `400 Bad Request (Request Blocked)` 에러가 발생하며 프로세스 중단.
- **원인**: 
  - KOPIS API의 목록 API에는 티켓 가격 정보가 없어 단건 상세 API를 N+1 형태로 수만 번 반복 호출해야 하는 구조적 한계 존재.
  - 고속 수집을 위해 멀티스레드 요청을 보낼 때, KOPIS 서버 측 WAF(방화벽)가 순간 트래픽 폭증을 DDoS 공격으로 인식하여 IP 및 요청 차단.

#### 2. 원인 분석 및 해결책 (Analysis & Solution)
- **WAF 차단 한계 속도 도출**: KOPIS WAF 안전임계치(초당 약 3~4회 이하)를 산출하여 스레드당 300ms 안전 대기 시간(`Thread.sleep(300)`) 설정.
- **2단계 자동 재시도(Retry) 메커니즘 구축**:
  - **목록 API**: WAF 차단 발생 시 즉시 종료하지 않고, 1.5초 쿨다운 대기 후 최대 3회 자동 재시도.
  - **상세 API**: 단건 조회 실패 시 1초 쿨다운 후 최대 3회 재시도하여 수집 안정성 확보.
- **멱등성(Idempotency) 기반 Fast-Skip 구현**:
  - `existsByExternalId` DB 사전 조회를 통해 이미 저장된 공연 데이터는 네트워크 통신 및 딜레이를 0.001초 만에 스킵하도록 처리.
  - 서버 재가동 및 중단 후 재실행 시 중복 수집 없이 멈춘 시점부터 즉시 이어받기 가능.

#### 3. 성과 및 결론 (Outcome)
- **수집 안정성 100% 확보**: WAF 차단에 의한 중간 다운 현상 완전 제거.
- **배치 파이프라인 완성**: 최초 1회 전수 적재 후, 일일 스케줄러를 통한 증분(Delta) 데이터 자동 갱신 파이프라인 구축.



### 🛠️ 트러블슈팅: KOPIS 외부 API 대용량 동기화 및 WAF(방화벽) 차단 극복 - 2026.11.12
#### 1. 다중 서버(Scale-Out) 환경의 세션 불일치 해결 (Session → JWT + Redis)

* **문제 상황 (Problem)**
  * 트래픽 분산을 위해 백엔드 인스턴스를 2대(`8080`, `8081`)로 확장하고 프론트엔드 로드밸런싱을 적용한 후, 한 서버에서 로그인해도 다른 서버로 API 요청이 라우팅되면 인증이 풀리는 **세션 불일치(Session Inconsistency)** 문제 발생.
* **원인 분석 (Cause)**
  * 기존 `HttpSession` 방식은 각 Tomcat 인스턴스의 로컬 메모리에 사용자 상태를 저장하므로, 별도의 세션 클러스터링 없이는 서버 간 로그인 상태를 공유할 수 없음.
* **해결 방법 (Solution)**
  * **Stateless JWT 인증 도입:** `JwtProvider`와 `OncePerRequestFilter` 기반의 `JwtAuthenticationFilter`를 구현하여 서버 메모리 의존성을 제거.
  * **Redis 기반 하이브리드 상태 관리:** JWT의 단점(발급 후 강제 만료 불가)을 보완하기 위해 공용 **Redis**를 도입. `RT:{memberId}`로 Refresh Token을 관리하고, 로그아웃 시 남은 유효시간만큼 `BL:{accessToken}` 블랙리스트를 등록해 두 인스턴스 모두 즉시 접근을 차단하도록 구현.
  * **커스텀 어노테이션으로 결합도 완화:** `@LoginMember`와 `HandlerMethodArgumentResolver`를 구현하여, 컨트롤러가 인증 방식(세션/토큰)을 몰라도 파라미터로 `memberId`를 직접 주입받도록 리팩토링.
* **결과 (Result)**
  * `8080`, `8081` 어느 인스턴스로 요청이 분산되어도 동일한 인증 상태를 유지하며, 로그아웃 즉시 모든 서버에서 해당 토큰이 무효화됨.

---

#### 2. 소셜 로그인 성공 후 장바구니 API 호출 시 `401 (A001)` 인증 실패

* **문제 상황 (Problem)**
  * 카카오·네이버 로그인 완료 후 브라우저 `localStorage`에 `accessToken`이 정상 저장되었음에도, 장바구니 조회(`GET /api/v1/cart`) 요청 시 `401 Unauthorized (A001: 로그인이 필요한 서비스입니다)` 에러 반환.
* **원인 분석 (Cause)**
  * 프론트엔드 `axiosClient`의 요청 인터셉터(`interceptors.request`)가 다중 서버 포트(`8080`/`8081`) 분기 처리만 수행하고, 스토리지에 저장된 토큰을 `Authorization` 헤더에 주입하는 로직이 누락됨.
  * 또한 `Header.jsx`의 로그아웃 핸들러가 구버전 키(`token`)만 삭제하고 백엔드 블랙리스트 API(`/api/v1/auth/logout`)를 호출하지 않는 문제 확인.
* **해결 방법 (Solution)**
  * `axiosClient` 요청 인터셉터에서 `localStorage`의 `accessToken`을 조회해 `Authorization: Bearer <token>` 헤더를 자동으로 첨부하도록 수정.
  * 로그아웃 시 백엔드 블랙리스트 등록 API를 선행 호출한 뒤 `finally` 블록에서 `accessToken`, `refreshToken`, `nickname`을 일괄 삭제하도록 동기화.

```javascript
// axiosClient.jsx - 로드밸런싱 및 JWT 헤더 자동 첨부
axiosClient.interceptors.request.use((config) => {
    const randomIndex = Math.floor(Math.random() * targetServers.length);
    config.baseURL = targetServers[randomIndex];

    const token = localStorage.getItem("accessToken");
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});
```

---

#### 3. 장바구니 담기 요청 시 `500 (C002)` 서버 내부 오류 및 DB 스키마 충돌

* **문제 상황 (Problem)**
  * `Authorization` 헤더 정상 전송 이후 `POST /api/v1/cart` 요청 시 `500 Internal Server Error (C002)` 발생.
* **원인 분석 (Cause)**
  1. **컨트롤러 레거시 코드 잔존:** `CartController`가 여전히 비어 있는 `HttpSession`에서 `memberId`를 조회하여 `NullPointerException` 발생.
  2. **DB 스키마 컬럼 충돌:** 과거 Hibernate `ddl-auto: update` 환경에서 오타로 생성된 `memeberId` 컬럼(`NOT NULL`, 기본값 없음)이 정상 컬럼인 `memberId`와 테이블에 공존하면서 `INSERT` 쿼리 제약조건 위반 발생.
* **해결 방법 (Solution)**
  * `CartController`의 모든 엔드포인트에서 `HttpSession`을 제거하고 `@LoginMember Long memberId`로 통일.
  * MySQL `cart` 테이블에서 레거시 오타 컬럼을 제거하여 엔티티 매핑과 DB 스키마를 일치시킴.

```sql
ALTER TABLE cart DROP COLUMN memeberId;
```

---

#### 4. DB 저장 성공 후 장바구니 화면 빈 목록 출력 문제

* **문제 상황 (Problem)**
  * MySQL `cart` 테이블에 데이터가 정상 적재되고 조회 API도 `200 OK`를 반환했으나, UI에는 "장바구니에 담긴 공연이 없습니다"가 표시됨.
* **원인 분석 (Cause)**
  * `axiosClient` 응답 인터셉터에서 이미 `response.data`를 언래핑(Unwrapping)하여 반환하고 있었으나, `CartPage.jsx`에서 응답 변수명 불일치(`ReferenceError: res is not defined`) 및 데이터 추출 깊이가 어긋나 배열 데이터가 `undefined`로 처리됨.
* **해결 방법 (Solution)**
  * `CartPage.jsx`의 비동기 응답 변수명과 `Array.isArray(res.data)` 검증 로직을 일원화하여 정상 렌더링 복구.

---

### 📊 트러블슈팅 요약

| 구분 | 증상 | 핵심 원인 | 해결 조치 |
| :--- | :--- | :--- | :--- |
| **인프라/인증** | 다중 서버 간 로그인 풀림 | 서버 로컬 `HttpSession` 사용 | JWT + Redis(RT/블랙리스트) + `@LoginMember` 도입 |
| **프론트 통신** | 토큰 보유 중 `401 (A001)` | Axios 인터셉터 헤더 누락 | 요청 인터셉터 `Bearer` 토큰 자동 주입 및 로그아웃 API 연동 |
| **백엔드/DB** | 장바구니 담기 `500 (C002)` | 세션 잔존 코드 & `memeberId` 오타 컬럼 | 컨트롤러 어노테이션 교체 및 DB 레거시 컬럼 `DROP` |
| **UI 렌더링** | `200 OK` 후 빈 화면 | 인터셉터 응답 구조와 컴포넌트 변수 불일치 | `CartPage` 응답 파싱 로직 통일 |
