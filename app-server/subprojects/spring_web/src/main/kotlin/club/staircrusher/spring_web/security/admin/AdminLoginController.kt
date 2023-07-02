package club.staircrusher.spring_web.security.admin

import club.staircrusher.admin_api.spec.dto.LoginPostRequest
import club.staircrusher.spring_web.security.SccSecurityFilterChainConfig
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

// TODO: admin_auth 모듈로 쪼개져야 함.
@RestController
class AdminLoginController(
    private val adminAuthenticationService: AdminAuthenticationService,
) {
    @PostMapping("/admin/login")
    fun login(@RequestBody request: LoginPostRequest): ResponseEntity<String> {
        val accessToken = try {
            adminAuthenticationService.login(
                username = request.username,
                password = request.password,
            )
        } catch (e: AdminAuthenticationException) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(e.message)
        }
        return ResponseEntity
            .noContent()
            .header(SccSecurityFilterChainConfig.accessTokenHeader, accessToken)
            .build()
    }
}
