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
