package club.staircrusher.user.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.LoginPostRequest
import club.staircrusher.api.spec.dto.LoginResultDto
import club.staircrusher.api.spec.dto.LoginWithKakaoPostRequest
import club.staircrusher.api.spec.dto.SignUpPostRequest
import club.staircrusher.spring_web.security.SccSecurityFilterChainConfig
import club.staircrusher.user.application.port.`in`.use_case.LoginWithKakaoUseCase
import club.staircrusher.user.application.port.`in`.UserApplicationService
import club.staircrusher.user.infra.adapter.`in`.converter.toDTO
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
    private val userApplicationService: UserApplicationService,
    private val loginWithKakaoUseCase: LoginWithKakaoUseCase,
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
            .header(SccSecurityFilterChainConfig.accessTokenHeader, result.accessToken)
            .build()
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginPostRequest): ResponseEntity<Unit> {
        val result = userApplicationService.login(
            nickname = request.nickname,
            password = request.password,
        )
        return ResponseEntity
            .noContent()
            .header(SccSecurityFilterChainConfig.accessTokenHeader, result.accessToken)
            .build()
    }

    @PostMapping("/loginWithKakao")
    fun login(@RequestBody request: LoginWithKakaoPostRequest): LoginResultDto {
        return loginWithKakaoUseCase.handle(request.kakaoTokens.idToken).toDTO()
    }
}
