# SCC Server

계단정복지도 서비스의 서버 코드 레포지토리.

## Set Up

### API Specification Build

이 레포지토리는 [scc-api 레포지토리](https://github.com/Stair-Crusher-Club/scc-api)에 선언된 API 명세를 사용합니다. 구체적으로, scc-api 레포지토리를 subprojects/api/scc-api 경로 아래에 submodule로 가진 채 로컬에서 [OpenAPI Generator](https://openapi-generator.tech/docs/generators/kotlin/)를 활용하여 코드를 생성한 후 사용합니다.

따라서 올바르게 프로젝트를 빌드하기 위해서는 OpenAPI Generator task를 실행하는 것이 필요합니다. 아래 커맨드를 입력하면 되며, 이 과정은 API 명세를 최신화할 때마다 필요합니다.

```bash
gradle openApiGenerate
# 혹은
./gradlew openApiGenerate
```

프론트엔드(어드민)를 위한 typescript generating은 아래의 커맨드를 실행해야 합니다.
```bash
# admin-frontend 디렉토리에서
./generate-api-spec.sh
```

### 로컬에서 빌드하기 / 서버 띄우기

로컬에서 빌드하거나 서버를 띄우기 위해서는 DB가 필요합니다.
로컬에서 빌드할 때 테스트가 사용하는 DB를 띄우려면 아래 커맨드를 실행하면 됩니다.
```bash
./run-database.sh
```

위 커맨드는 `scc_test`라는 데이터베이스를 생성합니다. 로컬에서 서버를 띄우려면 `scc`라는 데이터베이스가 필요한데, 이는 직접 생성해줘야 합니다.
```bash
❯ psql -h localhost -p 15432 -U test -d scc_test -W
Password: 
psql (14.5 (Homebrew))
Type "help" for help.

scc_test= create database scc;
```
