package club.staircrusher.spring_web.security

import org.springframework.stereotype.Component

@Component
class EchoUserIdSecurityConfig : SccSecurityConfig {
    override fun getAuthenticatedUrls() = listOf("/echoUserId/secured")
}
