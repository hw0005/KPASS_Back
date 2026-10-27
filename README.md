## 🛠️ 트러블슈팅 및 아키텍처 개선 (Troubleshooting) - 2026.10.27

### 1. 서비스 로직(BO) 내 검증 코드 중복 및 단일 책임 원칙(SRP) 위배 개선
* **문제 상황**: `MemberBO` 내에 회원가입/로그인 비즈니스 로직과 DB 중복 검증 로직이 섞여 코드 가독성이 떨어지고 객체 책임이 비대해짐.
* **해결 방법**:
  * DB 검증 전담 객체인 `MemberValidator`를 별도로 분리하여 검증 책임을 이관.
  * `MemberBO`는 순수한 비즈니스 흐름 제어에만 집중하도록 리팩토링.
* **성과**: BO 객체의 단일 책임 원칙(SRP) 준수 및 검증 로직의 재사용성 확보.

### 2. 예외 처리 일관성 확보 및 커스텀 에러 응답 표준화
* **문제 상황**: HTTP 400(Bad Request) 응답만으로는 '이메일 중복'과 '비밀번호 불일치' 등 프론트엔드가 세부 에러 원인을 구별할 수 없음.
* **해결 방법**:
  * `HttpStatus`, 커스텀 코드(`M001`~`M004`), 에러 메시지를 캡슐화한 `ErrorCode` Enum 구현.
  * `BusinessException`과 `GlobalExceptionHandler`를 통해 통일된 JSON 에러 구조 응답.
  * 흐름: 에러 발생 -> GLoablalExceptionHandler && Advice가 잡고-> BusinessException -> errorCode가져와서 저장 
* **성과**: 프론트엔드에서 `code`값(`M001`, `M004` 등)에 따라 특정 input 창 하단에 정확한 에러 메시지를 분기 출력 가능.
