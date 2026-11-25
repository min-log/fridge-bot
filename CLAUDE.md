# CLAUDE.md

AI(Claude Code)와 함께 개발할 때 매번 먼저 읽히는 작업 규칙이다. 사람이 정한 규칙이며, 바꿀 때는 PR로 바꾼다.

## 먼저 읽을 문서
- [docs/00_기획.md](docs/00_기획.md): 왜 만드는지, 범위, 하지 않을 것
- [docs/01_설계.md](docs/01_설계.md): 기술 선택, DB, API, 주차별 일정
- [docs/adr/](docs/adr/README.md): 구현 중 정한 결정. 이미 정한 것을 다시 바꾸려면 새 ADR을 제안한다
- [docs/ai/작업방식.md](docs/ai/작업방식.md): 작업 지시 형식과 완료 조건

## 프로젝트 구조
- `backend/`: Spring Boot 4.1 (Java 25, Gradle). 패키지는 기능 단위 (`auth`, `user`, `household`, `common`, ...)
- `frontend/`: React 19 + Vite PWA (4주차에 추가)
- `docs/`: 공개 문서. `docs/private/`는 커밋하지 않는다

## 명령
```bash
cd backend
./gradlew check        # 테스트 + JaCoCo (Docker 필요: Testcontainers)
./gradlew llmTest      # 실제 LLM API 테스트, 수동 실행만
```

## 코드 규칙
- 업무 규칙은 엔티티·도메인 객체에 두고 단위 테스트한다. 서비스는 조회·저장·트랜잭션을 맡는다
- `open-in-view: false`. 지연 로딩 필드는 서비스 트랜잭션 안에서 응답 객체로 바꾼다
- 오류는 `ApiException` + 고정 `code` (ProblemDetail, ADR 0007)
- 가구 범위 데이터는 요청에서 household_id를 받지 않는다 (ADR 0006)
- 스키마 변경은 Flyway 새 버전 파일로만 한다. 이미 적용된 마이그레이션은 고치지 않는다
- 통합 테스트는 `IntegrationTestSupport`를 상속하고 테스트마다 새 사용자를 만든다

## 비밀값
- `application.yml`에 키·접속 정보의 기본값을 쓰지 않는다 (ADR 0001)
- 실제 키를 코드·문서·테스트·커밋 메시지에 넣지 않는다. 테스트 값은 `application-test.yml`의 가짜 값만 쓴다

## 공개 문서 규칙 (ADR 0009)
- 저장소는 공개다. 문서에는 결정과 이유를 중립적으로 쓴다
- 회고와 개인 메모는 `docs/private/`에만 둔다. `docs/`에 그런 파일이 보이면 커밋하지 말고 알린다
- 기록은 사실만 쓴다. AI가 제안하지 않은 것을 "AI가 제안했다"고 쓰거나, 사람이 고치지 않은 것을 고쳤다고 쓰지 않는다

## 작업 마무리
- 기능마다 브랜치 → PR. PR 설명은 `.github/pull_request_template.md`를 채운다 (AI 활용, 검증 포함)
- AI 결과에서 틀린 것을 발견하면 [docs/ai/검증기록.md](docs/ai/검증기록.md)에 남긴다
- 측정한 숫자는 [docs/결과.md](docs/결과.md)에 남긴다
- 새 기술 결정은 `docs/adr/`에 ADR로 남긴다
