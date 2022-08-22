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
