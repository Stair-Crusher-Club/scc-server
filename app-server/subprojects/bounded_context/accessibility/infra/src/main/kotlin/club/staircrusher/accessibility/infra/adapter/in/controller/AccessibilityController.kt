package club.staircrusher.accessibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.application.port.`in`.AccessibilityApplicationService
import club.staircrusher.accessibility.application.port.`in`.GetImageUploadUrlsUseCase
import club.staircrusher.accessibility.application.port.`in`.RegisterBuildingAccessibilityUseCase
import club.staircrusher.accessibility.application.port.`in`.RegisterPlaceAccessibilityUseCase
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityCommentRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityCommentRepository
import club.staircrusher.api.converter.toDTO
import club.staircrusher.api.spec.dto.AccessibilityInfoDto
import club.staircrusher.api.spec.dto.GetAccessibilityPostRequest
import club.staircrusher.api.spec.dto.GetImageUploadUrlsPost200ResponseInner
import club.staircrusher.api.spec.dto.GetImageUploadUrlsPostRequest
import club.staircrusher.api.spec.dto.RegisterBuildingAccessibilityRequestDto
import club.staircrusher.api.spec.dto.RegisterPlaceAccessibilityRequestDto
import club.staircrusher.api.spec.dto.RegisterPlaceAccessibilityResponseDto
import club.staircrusher.spring_web.security.app.SccAppAuthentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AccessibilityController(
    private val accessibilityApplicationService: AccessibilityApplicationService,
    private val getImageUploadUrlsUseCase: GetImageUploadUrlsUseCase,
    private val registerPlaceAccessibilityUseCase: RegisterPlaceAccessibilityUseCase,
    private val registerBuildingAccessibilityUseCase: RegisterBuildingAccessibilityUseCase,
) {
    @PostMapping("/getAccessibility")
    fun getAccessibility(
        @RequestBody request: GetAccessibilityPostRequest,
        authentication: SccAppAuthentication?,
    ): AccessibilityInfoDto {
        return accessibilityApplicationService.getAccessibility(request.placeId, authentication?.details?.id)
            .toDTO(authentication?.details)
    }

    @PostMapping("/getImageUploadUrls")
    fun getImageUploadUrls(@RequestBody request: GetImageUploadUrlsPostRequest): List<GetImageUploadUrlsPost200ResponseInner> {
        return getImageUploadUrlsUseCase.handle(
            urlCount = request.count,
            filenameExtension = request.filenameExtension,
        ).map {
            GetImageUploadUrlsPost200ResponseInner(
                it.url,
                it.expireAt.toDTO(),
            )
        }
    }

    @PostMapping("/registerPlaceAccessibility")
    fun registerPlaceAccessibility(
        @RequestBody request: RegisterPlaceAccessibilityRequestDto,
        authentication: SccAppAuthentication,
    ): RegisterPlaceAccessibilityResponseDto {
        val userId = authentication.principal
        val (registerResult, getAccessibilityResult) = registerPlaceAccessibilityUseCase.handle(
            userId = authentication.principal,
            createPlaceAccessibilityParams = request.toModel(userId = userId),
            createPlaceAccessibilityCommentParams = request.comment?.let {
                PlaceAccessibilityCommentRepository.CreateParams(
                    placeId = request.placeId,
                    userId = userId,
                    comment = it,
                )
            },
        )
        return RegisterPlaceAccessibilityResponseDto(
            accessibilityInfo = getAccessibilityResult.toDTO(authentication.details),
            registeredUserOrder = registerResult.registrationOrder,
            // contributedChallenges = listOf() // TODO: 내가 참여하는 챌린지 중 만족하는 challenge 내려주기
        )
    }

    @PostMapping("/registerBuildingAccessibility")
    fun registerBuildingAccessibility(
        @RequestBody request: RegisterBuildingAccessibilityRequestDto,
        authentication: SccAppAuthentication,
    ) {
        val userId = authentication.principal
        registerBuildingAccessibilityUseCase.handle(
            userId = userId,
            createBuildingAccessibilityParams = request.toModel(userId = userId),
            createBuildingAccessibilityCommentParams = request.comment?.let {
                BuildingAccessibilityCommentRepository.CreateParams(
                    buildingId = request.buildingId,
                    userId = userId,
                    comment = it,
                )
            },
        )
        // contributedChallenges = listOf() // TODO: 내가 참여하는 챌린지 중 만족하는 challenge 내려주기
    }
}
