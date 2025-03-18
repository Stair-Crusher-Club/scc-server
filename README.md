# SCC Server

계단정복지도 서비스의 서버 코드 레포지토리.

## Set Up

### Build API Protocol files

이 레포지토리는 [scc-api 레포지토리](https://github.com/Stair-Crusher-Club/scc-api)에 선언된 API 명세를 사용합니다.
구체적으로, scc-api 레포지토리를 subprojects/api/scc-api 경로에 subtree 로 가집니다.

로컬에서 [OpenAPI Generator](https://openapi-generator.tech/docs/generators/kotlin/)를 활용하여 코드를 생성한 후 사용합니다.
따라서 올바르게 프로젝트를 빌드하기 위해서는 OpenAPI Generator task를 실행하는 것이 필요합니다.

app-server 경로에서 아래 커맨드를 입력하면 api-spec 에 따라 Kotlin 파일들이 만들어집니다.
```bash
gradle openApiGenerate
# 혹은
./gradlew openApiGenerate
```

### Update API Protocol Specification

기능 개발 시에 프로토콜 변경이 필요하면 scc-api/api-spec.yml 내용 수정 후 아래와 같이 subtree push 를 이용하여 scc-api 에 변경사항을 반영합니다.
```bash
// subtree remote 변경사항 반영하기
git subtree pull --prefix app-server/subprojects/api/scc-api scc-api main
// scc-api main branch 에 push 하기
git subtree push --prefix app-server/subprojects/api/scc-api scc-api main
```

### 로컬에서 빌드하기 / 서버 띄우기

로컬에서 빌드하거나 서버를 띄우기 위해서는 DB가 필요합니다.
DB를 띄우려면 아래 커맨드를 실행하면 됩니다.
```bash
docker compose up -d
```
