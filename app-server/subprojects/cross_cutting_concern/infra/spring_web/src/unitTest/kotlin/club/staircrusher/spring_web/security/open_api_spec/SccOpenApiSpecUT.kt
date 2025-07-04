package club.staircrusher.spring_web.security.open_api_spec

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod

class SccOpenApiSpecUT {
    @Test
    fun `일반적인 경우`() {
        val openApiSpecYaml = """
openapi: '3.0.0'
info:
  title: Stair Crusher Club API Specification
  version: '1.0.0'
  description: |
    ## 계단정복지도 서비스의 서버 - 클라이언트 통신을 위한 API 명세.

defaultContentType: application/json

servers:
  - url: https://{env}.staircrusher.club/
    variables:
      env:
        description: API 주소 환경 설정
        default: 'api'
        enum:
          - 'api'
          - 'api.dev'

security:
  # 모든 API 는 기본적으로 AnonymousUserAuth 를 적용
  - Anonymous: []

paths:
  /cancelBuildingAccessibilityUpvote:
    post:
      security:
        - Identified: []
      requestBody:
        content:
          application/json:
            schema:
              \$${"ref"}: '#/components/schemas/CancelBuildingAccessibilityUpvoteRequestDto'
        required: true
      responses:
        '204': {}
      summary: "'이 정보가 도움이 돼요'를 취소한다."

  /cancelPlaceAccessibilityUpvote:
    post:
      security:
        - Identified: []
      requestBody:
        content:
          application/json:
            schema:
              \$${"ref"}: '#/components/schemas/CancelPlaceAccessibilityUpvoteRequestDto'
        required: true
      responses:
        '204': {}
      summary: "'이 정보가 도움이 돼요'를 취소한다."
        """.trimIndent()

        val sut = SccOpenApiSpec(openApiSpecYaml)
        Assertions.assertEquals(2, sut.paths.size)

        Assertions.assertEquals("/cancelBuildingAccessibilityUpvote", sut.paths[0].url)
        Assertions.assertEquals(HttpMethod.POST, sut.paths[0].method)
        Assertions.assertEquals(listOf(SccOpenApiSpecSecurityType.IDENTIFIED), sut.paths[0].securityTypes)
        Assertions.assertTrue(sut.paths[0].isIdentifiedUserOnly)
    }

    @Test
    fun `default security가 잘 먹는지 확인`() {
        val openApiSpecYaml = """
openapi: '3.0.0'
info:
  title: Stair Crusher Club API Specification
  version: '1.0.0'
  description: |
    ## 계단정복지도 서비스의 서버 - 클라이언트 통신을 위한 API 명세.

defaultContentType: application/json

servers:
  - url: https://{env}.staircrusher.club/
    variables:
      env:
        description: API 주소 환경 설정
        default: 'api'
        enum:
          - 'api'
          - 'api.dev'

security:
  # 모든 API 는 기본적으로 AnonymousUserAuth 를 적용
  - Anonymous: []

paths:
  /getAccessibility:
    post:
      summary: 건물 & 점포의 접근성 정보를 조회한다.
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              properties:
                placeId:
                  type: string
              required:
                - placeId
      responses:
        '200':
          content:
            application/json:
              schema:
                \$${"ref"}: '#/components/schemas/AccessibilityInfoDto'
        """.trimIndent()

        val sut = SccOpenApiSpec(openApiSpecYaml)
        Assertions.assertEquals(1, sut.paths.size)

        Assertions.assertEquals("/getAccessibility", sut.paths[0].url)
        Assertions.assertEquals(HttpMethod.POST, sut.paths[0].method)
        Assertions.assertEquals(listOf(SccOpenApiSpecSecurityType.ANONYMOUS), sut.paths[0].securityTypes)
        Assertions.assertFalse(sut.paths[0].isIdentifiedUserOnly)
    }

    @Test
    fun `security가 명시적으로 emptyList인 경우`() {
        val openApiSpecYaml = """
openapi: '3.0.0'
info:
  title: Stair Crusher Club API Specification
  version: '1.0.0'
  description: |
    ## 계단정복지도 서비스의 서버 - 클라이언트 통신을 위한 API 명세.

defaultContentType: application/json

servers:
  - url: https://{env}.staircrusher.club/
    variables:
      env:
        description: API 주소 환경 설정
        default: 'api'
        enum:
          - 'api'
          - 'api.dev'

security:
  # 모든 API 는 기본적으로 AnonymousUserAuth 를 적용
  - Anonymous: []

paths:
  /createAnonymousUser:
    post:
      summary: 비회원 계정을 생성한다.
      security: []
      responses:
        '200':
          content:
            application/json:
              schema:
                \$${"ref"}: '#/components/schemas/CreateAnonymousUserResponseDto'
        """.trimIndent()

        val sut = SccOpenApiSpec(openApiSpecYaml)
        Assertions.assertEquals(1, sut.paths.size)

        Assertions.assertEquals("/createAnonymousUser", sut.paths[0].url)
        Assertions.assertEquals(HttpMethod.POST, sut.paths[0].method)
        Assertions.assertEquals(emptyList<SccOpenApiSpecSecurityType>(), sut.paths[0].securityTypes)
        Assertions.assertFalse(sut.paths[0].isIdentifiedUserOnly)
    }

    @Test
    fun `어드민 등 url prefix가 필요한 경우`() {
        val openApiSpecYaml = """
openapi: '3.0.0'
info:
  title: Stair Crusher Club Admin API Specification
  version: '1.0.0'
  description: |
    ## 계단정복지도 서비스의 서버 - 어드민 통신을 위한 API 명세.

defaultContentType: application/json

tags:
  - name: challenge
  - name: accessibility
  - name: banner

paths:
  /login:
    post:
      summary: 로그인을 한다.
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              properties:
                username:
                  type: string
                password:
                  type: string
              required:
                - username
                - password
      responses:
        '204':
          description: |
            빈 response body를 내려준다.
            대신 Header의 X-SCC-ACCESS-KEY에 access token을 내려준다.
            클라이언트는 이 토큰을 Bearer auth의 토큰으로 사용하면 된다.
        """.trimIndent()

        val sut = SccOpenApiSpec(openApiSpecYaml, urlPrefix = "/admin")
        Assertions.assertEquals(1, sut.paths.size)

        Assertions.assertEquals("/admin/login", sut.paths[0].url)
        Assertions.assertEquals(HttpMethod.POST, sut.paths[0].method)
        Assertions.assertEquals(emptyList<SccOpenApiSpecSecurityType>(), sut.paths[0].securityTypes)
        Assertions.assertFalse(sut.paths[0].isIdentifiedUserOnly)
    }

    @Test
    fun `path item에 parameters가 있는 경우`() {
        val openApiSpecYaml = """
openapi: '3.0.0'
info:
  title: Stair Crusher Club Admin API Specification
  version: '1.0.0'
  description: |
    ## 계단정복지도 서비스의 서버 - 어드민 통신을 위한 API 명세.

defaultContentType: application/json

tags:
  - name: challenge
  - name: accessibility
  - name: banner

paths:
  /accessibilityAllowedRegions/{regionId}:
    parameters:
      - in: path
        name: regionId
        schema:
          type: string
        required: true

    get:
      summary: 정보 등록 가능 지역을 조회한다.
      responses:
        '200':
          content:
            application/json:
              schema:
                \$${"ref"}: '#/components/schemas/AccessibilityAllowedRegionDTO'

    delete:
      summary: 정보 등록 가능 지역을 삭제한다.
      responses:
        '204': {}
        """.trimIndent()

        val sut = SccOpenApiSpec(openApiSpecYaml, urlPrefix = "/admin")
        Assertions.assertEquals(2, sut.paths.size)

        Assertions.assertEquals("/admin/accessibilityAllowedRegions/{regionId}", sut.paths[0].url)
        Assertions.assertEquals(HttpMethod.GET, sut.paths[0].method)
        Assertions.assertEquals(emptyList<SccOpenApiSpecSecurityType>(), sut.paths[0].securityTypes)
        Assertions.assertFalse(sut.paths[0].isIdentifiedUserOnly)
    }
}
