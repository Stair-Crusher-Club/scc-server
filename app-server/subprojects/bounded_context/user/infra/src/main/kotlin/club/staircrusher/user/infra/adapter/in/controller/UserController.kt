package club.staircrusher.user.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.GetUserInfoResponseDto
import club.staircrusher.api.spec.dto.UpdatePushTokenPostRequest
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
                "b68b714e-40a3-4e52-aff4-c8734181e1bb",
                "740289a3-7c16-4673-b204-58a8aef0e242",
                "5cd204fe-57fa-42ff-8f77-d4b558c6761f",
                "b23d1425-508b-4d18-b6e3-67bb9b5361cb",
                "7e4f56ce-d115-4395-86c8-827280635208",
                "5493241a-5f55-4205-bfa7-d156d980cb30",
                "5ff9557f-218a-4da7-9980-924b0cae143e",
                "545c7208-5156-4803-8b0b-9049290b1071",
                "329c87d7-9d88-48c6-89cb-a8b3cec6cc2d",
                "6e020626-3b0d-43ab-88cc-ded8a682c404",
                "f1e7adf9-2d89-4c51-a180-38794789716b",
                "db01ba4f-53a1-413f-b03d-d453e276e082",
                "6eb17fda-b2d0-4aa7-8195-211861676abf",
                "b96b00ee-778a-4929-a5aa-3a2f1f957bfd",
                "cdf0667c-c094-4f97-85f5-c8349e4d0642",
                "3f313fb9-227e-45ea-b654-214e477e62d8",
                "0baf586f-6ab5-4979-9d4a-0952c02995fe",
                "34754cc1-423f-4120-98b3-2ac60f65fb05",
                "ef9b03d6-6516-4718-8f3c-f7c68e60418e",
                "42e97c7f-9c81-4065-b2cf-3a14935b85b5",
                "bf7c68c8-3092-4d4c-8a64-61df33c1ac29",
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

    @PostMapping("/updatePushToken")
    fun updatePushToken(
        @RequestBody request: UpdatePushTokenPostRequest,
        authentication: SccAppAuthentication,
    ) {
        userApplicationService.updatePushToken(
            userId = authentication.principal,
            pushToken = request.pushToken,
        )
    }

    @PostMapping("/deleteUser")
    fun deleteUser(authentication: SccAppAuthentication): ResponseEntity<Unit> {
        userApplicationService.deleteUser(authentication.principal)
        return ResponseEntity.noContent().build()
    }
}
