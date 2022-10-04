package club.staircrusher.user.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.LoginPostRequest
import club.staircrusher.api.spec.dto.SignUpPostRequest
import club.staircrusher.spring_web.SccSecurityConfig
import club.staircrusher.user.application.user.UserApplicationService
import org.springframework.http.HttpRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
    private val userApplicationService: UserApplicationService,
) {
    @PostMapping("/signUp")
    fun signUp(@RequestBody request: SignUpPostRequest): ResponseEntity<Unit> {
        val result = userApplicationService.signUp(
            nickname = request.nickname,
            password = request.password,
            instagramId = request.instagramId,
        )
        return ResponseEntity
            .noContent()
            .header(SccSecurityConfig.accessTokenHeader, result.accessToken)
            .build()
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginPostRequest, httpRequest: HttpRequest): ResponseEntity<Unit> {
        val accessToken = httpRequest.headers.getFirst(SccSecurityConfig.accessTokenHeader)
        val result = userApplicationService.login(
            nickname = request.nickname,
            password = request.password,
        )
        return ResponseEntity
            .noContent()
            .header(SccSecurityConfig.accessTokenHeader, result.accessToken)
            .build()
    }
}
