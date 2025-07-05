package club.staircrusher.spring_web.security.open_api_spec

import org.springframework.http.HttpMethod
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher

data class SccOpenApiSpecPath(
    val url: String,
    val method: HttpMethod,
    val securityTypes: List<SccOpenApiSpecSecurityType>,
) {
    val isIdentifiedUserOnly = SccOpenApiSpecSecurityType.IDENTIFIED in securityTypes
    val isAnonymousUserAllowed = SccOpenApiSpecSecurityType.ANONYMOUS in securityTypes
    val isAdminAllowed = SccOpenApiSpecSecurityType.ADMIN in securityTypes

    fun toRequestMatcher(): RequestMatcher {
        return AntPathRequestMatcher(url, method.name())
    }
}
