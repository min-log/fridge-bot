# fridge-bot

영수증 사진을 올리면 AI가 품목·가격을 뽑아 가구(냉장고) 재고에 넣고, 유통기한이 다가오면 알려 주고, 남은 재료로 메뉴를 추천하는 PWA 서비스.

- 설계: [docs/01_설계.md](docs/01_설계.md)
- 구현 중 결정 기록(ADR): [docs/adr/](docs/adr/README.md)
- 진행 상황: 2주차 (로그인 · 가구)

## 구조

```
backend/    Spring Boot 4.1 (Java 25, Gradle)
frontend/   React 19 + Vite PWA (4주차에 추가)
docs/       설계, 결정 기록(adr/)
```

## 로컬 실행

필요한 것: JDK 25, Docker

```bash
cd backend
./gradlew test          # Testcontainers로 pgvector·Redis를 띄워 테스트
```

앱 실행: `.env.example`을 `.env`로 복사해 JWT·소셜 로그인 값을 채운 뒤, 같은 컨테이너로 실행한다 (별도 DB 설치 없음).

```bash
cd backend
set -a; source ../.env; set +a
./gradlew bootTestRun
```

실제 LLM API를 부르는 테스트(`@Tag("llm")`)는 CI에서 제외하고 `./gradlew llmTest`로 직접 실행한다.

## 비밀값

- `application.yml`에는 접속 정보·키의 기본값을 두지 않는다. 환경변수로만 받는다.
- `.env.example`을 복사해 `.env`를 만들어 쓴다. `.env*`(예시 파일 제외), `*.pem`, `*.key`는 커밋되지 않는다.
- 모든 PR에서 gitleaks로 비밀값 검사를 한다.
