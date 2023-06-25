package club.staircrusher.spring_web.security

import club.staircrusher.spring_web.security.app.SccAppAuthentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class EchoUserIdController {
    @GetMapping("/echoUserId")
    fun echoUserId(authentication: SccAppAuthentication?): String {
        return authentication?.principal ?: "No authentication found."
    }

    @GetMapping("/echoUserId/secured")
    fun securedEchoUserId(authentication: SccAppAuthentication): String {
        return authentication.principal
    }
}
