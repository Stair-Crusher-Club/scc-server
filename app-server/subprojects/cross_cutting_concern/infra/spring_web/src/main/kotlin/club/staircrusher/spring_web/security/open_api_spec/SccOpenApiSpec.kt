package club.staircrusher.spring_web.security.open_api_spec

import mu.KotlinLogging
import org.springframework.http.HttpMethod
import org.yaml.snakeyaml.Yaml

class SccOpenApiSpec(
    openApiSpecYaml: String,
    private val urlPrefix: String = "",
) {
    private val openApiSpec = Yaml().load<Map<String, *>>(openApiSpecYaml)

    private val defaultSecurityTypes = openApiSpec.parseSecurityTypes()

    val paths: List<SccOpenApiSpecPath> = (openApiSpec["paths"] as Map<String, *>)
        .flatMap { (url, rawPath) ->
            (rawPath as Map<String, *>).mapNotNull { (rawMethod, rawPathItem) ->
                SccOpenApiSpecPath(
                    url = "${urlPrefix}$url",
                    method = HttpMethod.valueOf(rawMethod.uppercase())
                        .takeIf { it in HttpMethod.values() } ?: return@mapNotNull null, // post 말고 parameters 같은 필드도 존재할 수 있다.
                    securityTypes = if ("security" in (rawPathItem as Map<String, *>)){
                        rawPathItem.parseSecurityTypes()
                    } else {
                        defaultSecurityTypes
                    }
                )
            }
        }
        .also { logger.info("SccOpenApiSpec paths detected: $it") }

    companion object {
        private fun Map<String, *>.parseSecurityTypes(): List<SccOpenApiSpecSecurityType> {
            return (this["security"] as? List<Map<String, *>>)
                ?.flatMap { it.keys.map { rawSecurity -> SccOpenApiSpecSecurityType.valueOf(rawSecurity.uppercase()) } }
                ?: emptyList()
        }

        fun fromResourcePath(openApiSpecYamlResourcePath: String) = SccOpenApiSpec(
            openApiSpecYaml = this::class.java
                .getResourceAsStream(openApiSpecYamlResourcePath)!!
                .bufferedReader()
                .use { it.readText() },
        )

        private val logger = KotlinLogging.logger {}
    }
}
