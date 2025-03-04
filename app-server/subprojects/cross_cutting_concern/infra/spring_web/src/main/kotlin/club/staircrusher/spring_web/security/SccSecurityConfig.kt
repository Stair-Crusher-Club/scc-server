package club.staircrusher.spring_web.security

import org.springframework.security.web.util.matcher.RequestMatcher

interface SccSecurityConfig {
    fun requestMatchers(): List<RequestMatcher>
    fun identifiedUserOnlyRequestMatchers(): List<RequestMatcher>
}
