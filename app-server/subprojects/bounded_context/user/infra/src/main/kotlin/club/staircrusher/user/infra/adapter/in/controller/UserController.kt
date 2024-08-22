package club.staircrusher.user.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.GetUserInfoResponseDto
import club.staircrusher.api.spec.dto.UpdateUserInfoPost200Response
import club.staircrusher.api.spec.dto.UpdateUserInfoPostRequest
import club.staircrusher.spring_web.security.app.SccAppAuthentication
import club.staircrusher.stdlib.env.SccEnv
import club.staircrusher.user.application.port.`in`.UserApplicationService
import club.staircrusher.user.application.port.`in`.use_case.GetUserUseCase
import club.staircrusher.user.infra.adapter.`in`.converter.toDTO
import club.staircrusher.user.infra.adapter.`in`.converter.toModel
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val userApplicationService: UserApplicationService,
    private val getUserUseCase: GetUserUseCase,
) {
    @GetMapping("/getUserInfo")
    fun getUserInfo(
        authentication: SccAppAuthentication,
    ): GetUserInfoResponseDto {
        val user = getUserUseCase.handle(authentication.principal)
        val isTargetUser = user.id in
            listOf(
                "1c8a528c-0b5f-4885-a9b3-b81309c364df",
                "baf04e8e-0597-4926-b3b3-c3ecf9e3544e",
                "19ef11a0-bc2e-4262-a55f-943aad394004",
                "21468ced-cc68-44be-936e-a50d40ff5481",
                "b68b714e-40a3-4e52-aff4-c8734181e1bb"
            )
        val isNotProd = SccEnv.getEnv() != SccEnv.PROD
        val featureFlags: List<String> =
            if (isTargetUser || isNotProd) listOf("MAP_VISIBLE", "TOILET_VISIBLE") else emptyList()
        return GetUserInfoResponseDto(
            user = user.toDTO(),
            flags = featureFlags,
        )
    }

    @PostMapping("/updateUserInfo")
    fun updateUserInfo(
        @RequestBody request: UpdateUserInfoPostRequest,
        authentication: SccAppAuthentication,
    ): UpdateUserInfoPost200Response {
        val updatedUser = userApplicationService.updateUserInfo(
            userId = authentication.principal,
            nickname = request.nickname,
            instagramId = request.instagramId,
            email = request.email,
            mobilityTools = request.mobilityTools.map { it.toModel() },
            isNewsLetterSubscriptionAgreed = request.isNewsLetterSubscriptionAgreed ?: false,
        )
        return UpdateUserInfoPost200Response(
            user = updatedUser.toDTO(),
        )
    }

    @PostMapping("/deleteUser")
    fun deleteUser(authentication: SccAppAuthentication): ResponseEntity<Unit> {
        userApplicationService.deleteUser(authentication.principal)
        return ResponseEntity.noContent().build()
    }
}
