# SCC Server

계단정복지도 서비스의 서버 코드 레포지토리.

## Set Up

### Build API Protocol files

이 레포지토리는 [scc-api 레포지토리](https://github.com/Stair-Crusher-Club/scc-api)에 선언된 API 명세를 사용합니다.
구체적으로, scc-api 레포지토리를 app-server/subprojects/api_specification/api/scc-api 경로에 submodule 로 가집니다.
따라서 빌드 전에 submodule 을 초기화 해줘야 합니다.
```sh
git submodule init
git submodule update
```

[OpenAPI Generator](https://openapi-generator.tech/docs/generators/kotlin/)를 활용하여 api-spec 에 대응하는 코드를 생성한 후 사용합니다.
따라서 올바르게 프로젝트를 빌드하기 위해서는 OpenAPI Generator task를 실행하는 것이 필요합니다.

app-server 경로에서 아래 커맨드를 입력하면 필요한 Kotlin 파일들이 만들어집니다.
```bash
gradle openApiGenerate
# 혹은
./gradlew openApiGenerate
```

### 로컬에서 빌드하기 / 서버 띄우기

로컬에서 빌드하거나 서버를 띄우기 위해서는 DB가 필요합니다.
DB를 띄우려면 아래 커맨드를 실행하면 됩니다.
```bash
docker compose up -d
```
