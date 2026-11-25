# 0007. 오류 응답은 RFC 9457 ProblemDetail과 고정 code로 통일한다

- 상태: 채택
- 날짜: 2026-11-25

## 배경
프론트가 오류를 구분해 다르게 안내해야 한다 (예: 초대 코드 만료 → "새 코드를 받아 주세요"). 오류마다 응답 형식이 다르면 처리 코드가 늘어난다.

## 결정
- 모든 오류는 `application/problem+json`(RFC 9457)으로 돌려준다.
- 업무 오류는 `ApiException`(Spring `ErrorResponseException` 상속)으로 던지고, 본문에 고정 문자열 `code`(예: `INVITE_CODE_EXPIRED`, `OWNER_ONLY`)를 넣는다. 프론트는 문구가 아니라 `code`로 분기한다.
- 요청 검증 오류는 `spring.mvc.problemdetails.enabled`로 같은 형식을 쓴다.
- 보안 필터에서 나는 401·403도 같은 형식으로 직접 써 준다.

## 검토한 대안
- 직접 정의한 `{success, message}` 형식: 표준이 아니고 Spring 기본 오류와 형식이 섞인다.

## 결과
- 상태 코드 + `code`만 보면 오류 종류를 알 수 있다.
- 새 오류를 추가할 때 `code` 이름을 정해야 한다.
