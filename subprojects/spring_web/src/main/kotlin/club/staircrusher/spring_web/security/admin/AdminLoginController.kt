package club.staircrusher.spring_web.security.admin

import club.staircrusher.admin_api.spec.dto.LoginPostRequest
import club.staircrusher.spring_web.security.SccSecurityFilterChainConfig
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

// TODO: bounded context 외에도 "어드민"이나 "앱" 이라는 관심사에 대한 모듈이 따로 존재해야 할 듯함.
//       일단은 적절한 위치가 없어서 여기에 배치함.
//       컨트롤러도 각 bounded context의 infra 모듈이 아니라 "어드민" / "앱" 모듈에 있어야 하나?
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
