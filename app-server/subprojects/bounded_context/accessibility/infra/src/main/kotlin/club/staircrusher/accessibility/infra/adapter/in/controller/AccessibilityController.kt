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
import club.staircrusher.api.spec.dto.RegisterAccessibilityPost200Response
import club.staircrusher.api.spec.dto.RegisterAccessibilityPostRequest
import club.staircrusher.api.spec.dto.RegisterBuildingAccessibilityRequestDto
import club.staircrusher.api.spec.dto.RegisterPlaceAccessibilityPost200Response
import club.staircrusher.api.spec.dto.RegisterPlaceAccessibilityRequestDto
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

    @PostMapping("/registerAccessibility")
    fun registerAccessibility(
        @RequestBody request: RegisterAccessibilityPostRequest,
        authentication: SccAppAuthentication,
    ): RegisterAccessibilityPost200Response {
        val userId = authentication.principal
        val result = accessibilityApplicationService.register(
            createBuildingAccessibilityParams = request.buildingAccessibilityParams?.toModel(userId = userId),
            createBuildingAccessibilityCommentParams = request.buildingAccessibilityParams?.comment?.let {
                BuildingAccessibilityCommentRepository.CreateParams(
                    buildingId = request.buildingAccessibilityParams!!.buildingId,
                    userId = userId,
                    comment = it,
                )
            },
            createPlaceAccessibilityParams = request.placeAccessibilityParams.toModel(userId = userId),
            createPlaceAccessibilityCommentParams = request.placeAccessibilityParams.comment?.let {
                PlaceAccessibilityCommentRepository.CreateParams(
                    placeId = request.placeAccessibilityParams.placeId,
                    userId = userId,
                    comment = it,
                )
            },
        )
        return RegisterAccessibilityPost200Response(
            buildingAccessibility = result.buildingAccessibility?.toDTO(
                isUpvoted = false,
                totalUpvoteCount = 0,
                registeredUserName = result.accessibilityRegisterer?.nickname,
            ),
            buildingAccessibilityComments = listOfNotNull(result.buildingAccessibilityComment).map {
                it.toDTO(accessibilityRegisterer = result.accessibilityRegisterer)
            },
            placeAccessibility = result.placeAccessibility.toDTO(
                registeredAccessibilityRegisterer = result.accessibilityRegisterer,
                authUser = authentication.details,
                isLastInBuilding = result.isLastPlaceAccessibilityInBuilding,
            ),
            placeAccessibilityComments = listOfNotNull(result.placeAccessibilityComment).map {
                it.toDTO(accessibilityRegisterer = result.accessibilityRegisterer)
            },
            registeredUserOrder = result.registrationOrder,
        )
    }

    @PostMapping("/registerPlaceAccessibility")
    fun registerPlaceAccessibility(
        @RequestBody request: RegisterPlaceAccessibilityRequestDto,
        authentication: SccAppAuthentication,
    ): RegisterPlaceAccessibilityPost200Response {
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

        return RegisterPlaceAccessibilityPost200Response(
            accessibilityInfo = getAccessibilityResult.toDTO(authentication.details),
            registeredUserOrder = registerResult.registrationOrder,
        )
    }

    @PostMapping("/registerBuildingAccessibility")
    fun registerBuildingAccessibility(
        @RequestBody request: RegisterBuildingAccessibilityRequestDto,
        authentication: SccAppAuthentication,
    ) {
        val userId = authentication.principal
        registerBuildingAccessibilityUseCase.handle(
            createBuildingAccessibilityParams = request.toModel(userId = userId),
            createBuildingAccessibilityCommentParams = request.comment?.let {
                BuildingAccessibilityCommentRepository.CreateParams(
                    buildingId = request.buildingId,
                    userId = userId,
                    comment = it,
                )
            },
        )
    }
}
