# 0002. 테스트와 로컬 실행은 Testcontainers로 운영과 같은 DB·Redis를 쓴다

- 상태: 채택
- 날짜: 2026-11-25

## 배경
운영은 PostgreSQL 17 + pgvector, Redis 8이다. 벡터 검색, `timestamptz`, 유니크 제약 같은 동작은 DB마다 다르다.

## 결정
- 통합 테스트는 `pgvector/pgvector:pg17`, `redis:8-alpine` 컨테이너를 `@ServiceConnection`으로 붙인다.
- 모든 통합 테스트는 `@IntegrationTest` 메타 애너테이션 하나로 같은 설정을 써서 Spring 컨텍스트와 컨테이너를 한 번만 띄운다.
- 테스트마다 새 사용자를 만들어 데이터가 겹치지 않게 하고, 테이블을 비우지 않는다.
- 로컬 실행도 `./gradlew bootTestRun`으로 같은 컨테이너 설정을 재사용한다. docker-compose는 배포 단계에서 작성한다.

## 검토한 대안
- H2 인메모리: 빠르지만 pgvector와 PostgreSQL 전용 SQL을 검증할 수 없다.
- 로컬용 docker-compose 따로 작성: 테스트 설정과 로컬 설정이 두 군데로 갈라진다.

## 결과
- CI(GitHub Actions)에서도 같은 컨테이너로 테스트한다. 전체 테스트는 컨테이너 기동 포함 약 15~20초.
- 테스트를 돌리려면 Docker가 필요하다.
