package club.staircrusher.spring_web

import club.staircrusher.spring_web.authentication.SccSecurityConfig
import org.springframework.stereotype.Component

@Component
class EchoUserIdSecurityConfig : SccSecurityConfig {
    override fun getAuthenticatedUrls() = listOf("/echoUserId/secured")
}
