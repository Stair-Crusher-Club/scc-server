package club.staircrusher.user.infra.adapter.`in`.controller

import club.staircrusher.admin_api.spec.dto.AdminSendPushNotificationRequestDto
import club.staircrusher.api.spec.dto.CheckNicknameDuplicationPost200Response
import club.staircrusher.api.spec.dto.CheckNicknameDuplicationPostRequest
import club.staircrusher.api.spec.dto.GetUserInfoResponseDto
import club.staircrusher.api.spec.dto.UpdatePushTokenPostRequest
import club.staircrusher.api.spec.dto.UpdateUserInfoPost200Response
import club.staircrusher.api.spec.dto.UpdateUserInfoPostRequest
import club.staircrusher.spring_web.security.admin.SccAdminAuthentication
import club.staircrusher.spring_web.security.app.SccAppAuthentication
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.stdlib.env.SccEnv
import club.staircrusher.user.application.port.`in`.UserApplicationService
import club.staircrusher.user.application.port.`in`.use_case.GetUserProfileUseCase
import club.staircrusher.user.infra.adapter.`in`.converter.toDTO
import club.staircrusher.user.infra.adapter.`in`.converter.toModel
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val userApplicationService: UserApplicationService,
    private val getUserProfileUseCase: GetUserProfileUseCase,
) {
    private val logger = KotlinLogging.logger {}

    @GetMapping("/getUserInfo")
    fun getUserInfo(
        authentication: SccAppAuthentication,
    ): GetUserInfoResponseDto {
        val userProfile = getUserProfileUseCase.handle(authentication.principal) ?: throw SccDomainException("잘못된 계정입니다")
        val isTargetUser = userProfile.userId in betaUsers
        val isNotProd = SccEnv.getEnv() != SccEnv.PROD
        val featureFlags: List<String> =
            if (isTargetUser || isNotProd) listOf("MAP_VISIBLE", "TOILET_VISIBLE") else listOf("TOILET_VISIBLE")
        return GetUserInfoResponseDto(
            user = userProfile.toDTO(),
            flags = featureFlags,
        )
    }

    @PostMapping("/updateUserInfo")
    fun updateUserInfo(
        @RequestBody request: UpdateUserInfoPostRequest,
        authentication: SccAppAuthentication,
    ): UpdateUserInfoPost200Response {
        logger.info { "[UpdateUserInfo]: user(${authentication.principal}), $request" }
        val updatedUser = userApplicationService.updateUserInfo(
            userId = authentication.principal,
            nickname = request.nickname,
            instagramId = request.instagramId,
            email = request.email,
            mobilityTools = request.mobilityTools.map { it.toModel() },
            isNewsLetterSubscriptionAgreed = request.isNewsLetterSubscriptionAgreed ?: false,
            birthYear = request.birthYear,
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

    @PostMapping("/admin/user/sendPushNotification")
    fun adminSendPushNotification(
        @RequestBody request: AdminSendPushNotificationRequestDto,
        @Suppress("UnusedPrivateMember") authentication: SccAdminAuthentication,
    ) {
        userApplicationService.sendPushNotification(
            userIds = request.userIds,
            title = request.notification.title,
            body = request.notification.body,
            deepLink = request.notification.deepLink,
        )
    }

    @PostMapping("/deleteUser")
    fun deleteUser(authentication: SccAppAuthentication): ResponseEntity<Unit> {
        userApplicationService.deleteUser(authentication.principal)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/checkUserProfileValidation")
    fun checkUserProfileValidation(
        @RequestBody request: CheckUserProfileValidationPostRequest,
    ): CheckUserProfileValidationPost200Response {
        val result = userApplicationService.validateUserProfile(
            nickname = request.nickname,
            email = request.email,
            userId = request.userId,
        )
        return CheckUserProfileValidationPost200Response(
            nicknameErrorMessage = result.nicknameErrorMessage,
            emailErrorMessage = result.emailErrorMessage,
        )
    }

    companion object {
        private val betaUsers = listOf(
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
            "bb6e7dbc-d016-4397-b01f-480e56bf1101",
            "b5fa0aa0-dbd4-496a-98ad-4ac849da0b5c",
            "e2ab99ba-f1ee-44e0-9e57-ef0a4badc3d7",
            "545c7208-5156-4803-8b0b-9049290b1071",
            "60edf8e1-d50b-4623-b7a4-061c2227ac68",
            "7e4f56ce-d115-4395-86c8-827280635208",
            "3cdf0409-1db4-4f4f-b1bd-92a818693c2a",
            "44162b69-9491-4cc5-bfb3-279462dc75d3",
            "ef9b03d6-6516-4718-8f3c-f7c68e60418e",
            "f73ad931-f1b8-47fa-90cc-b84a34b3a636",
            "1bb2d8ba-f945-469b-87dd-906f52a674af",
            "00b01fff-1297-4f73-bc29-a797b44cb5c6",
            "3f3d21e2-4663-4474-a661-31e872a380a9",
            "1a9de1cd-4a62-4c71-8e3f-c16aaca2f3e3",
            "e8cac3bc-5175-4c03-8e1f-19ba2ae6e0eb",
            "5ff9557f-218a-4da7-9980-924b0cae143e",
            "ca7e4ed7-07b4-47b8-b8b8-a71ba7fadc7f",
            "fe3fd7bb-3697-4daa-bd5a-1a231682c61f",
            "dbefcacb-783f-481a-a615-cbb98a6023c3",
            "118135c1-4321-4972-a0ae-d45e92202ab3",
            "c7d3039f-71f7-4a53-b870-5cd6a3e6feb8",
            "32564f0a-e8a9-43f0-b83d-e110f4aea868",
            "2caaf7ed-0f53-4d60-9f65-ea21e09d3654",
            "a9a46a9e-9347-4351-ab71-484598d3423c",
            "a7db79f7-e158-4e3a-b892-9b48de1d9e70",
            "084f5097-2a45-4537-a203-0c0cfe3b731b",
            "55219235-55e5-4daf-828c-35e6469769cb",
            "f1e7adf9-2d89-4c51-a180-38794789716b",
            "5770771d-a4de-4d16-818a-b1459219fc85",
            "93e5bc3d-ab97-4066-9323-7ddaab255ec4",
            "8a76a46b-8097-4439-a351-4c79141a5526",
            "a9821a2e-27ad-4a44-9f86-6184f62779e3",
            "1bb2d8ba-f945-469b-87dd-906f52a674af",
            "3401df71-1ec1-4609-a683-03199be4255b",
            "a4a2b36e-0495-40ff-a008-22c9a0ca0fdc",
            "9d823d74-28fa-4214-bfd2-aa4ba2a3fbfc",
            "4138c1e8-f05f-419d-8a10-43cd15b7650c",
            "6a7fc8f2-a43a-4442-aeb7-75018aeb8bfa",
            "fd70342f-c665-412b-8a50-17990ca06151",
            "6cc556d6-2a15-45b8-9261-3c9c9c5f7d2b",
            "373c3b26-4bb7-48a7-972f-5525d87551e3",
            "9d3fea53-4594-4001-97ab-0618f826555e",
            "4d7d73e7-e9b3-42a5-b6d3-0e2a6e8605fe",
            "3cdf0409-1db4-4f4f-b1bd-92a818693c2a",
            "63c1229f-654d-4ac9-9d1f-55e5cf5006a7",
            "964c4a02-8018-4159-bcd3-ac46720dd109",
            "2b41a255-703e-44af-8004-e4a03c0fa373",
            "12443db6-c38d-45df-a27b-fe60f4e6cf73",
            "4bcb25b4-6225-4eca-8510-f93cac893d93",
            "db01ba4f-53a1-413f-b03d-d453e276e082",
            "ea450185-52a6-4a9c-868c-21b5b452babd",
            "d77ae0a3-fd96-413e-ae3f-64e0f6362ea0",
            "17f54bb1-1fb7-484b-8776-8dc1c14971cf",
            "5d6212c6-5b60-4e6a-ac78-028df6cd4ea1",
            "da680c03-9c15-4d82-846b-f54c14162cb7",
            "2a8787d3-4f12-4d0c-94fc-955a5fdf28e0",
            "fede98af-6a9c-42c5-8ab9-05a6aa0b8c3b",
            "ea3959af-456b-4eef-bab3-80a4a1438eb3",
            "a782061f-b7fb-4887-871d-51ef7e2a0422",
            "5e2f7c94-e7b0-4181-a977-16e1c3b00744",
            "3032725d-be17-42ab-aa83-57004b60e78c",
            "14e2da13-6f79-4b5c-ae73-c5fbf2404f08",
            "9497b270-73a7-4519-a041-f8ee9b41bd4e",
            "8c8c1b74-4554-48d9-a795-0adf1ca79633",
            "5331a953-fa87-4138-a820-d076d45e2873",
            "8130e3c5-4dcc-4f11-887e-d785d434fdf6",
            "b81ffc82-a8b2-41a7-8c68-65811a560972",
            "407a3a0e-1c51-4491-90ec-cc1e52a1ebf6",
            "13add2e4-c2fc-419b-a19b-f0a873de5113",
            "4be9d41a-b869-45e7-9fc0-a23f5357a191",
            "54c413b8-68f5-4bbd-b625-a2753236b5e0",
            "140e9f10-4890-4d55-b594-fa2f3c259864",
            "3fa51d8e-72cb-4776-834f-4fd0af87b0f1",
        )
    }
}
