package club.staircrusher.accessibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.application.port.`in`.AccessibilityApplicationService
import club.staircrusher.accessibility.application.port.`in`.GetImageUploadUrlsUseCase
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityCommentRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityCommentRepository
import club.staircrusher.api.converter.toDTO
import club.staircrusher.api.spec.dto.GetAccessibilityPost200Response
import club.staircrusher.api.spec.dto.GetAccessibilityPostRequest
import club.staircrusher.api.spec.dto.GetImageUploadUrlsPost200ResponseInner
import club.staircrusher.api.spec.dto.GetImageUploadUrlsPostRequest
import club.staircrusher.api.spec.dto.RegisterAccessibilityPost200Response
import club.staircrusher.api.spec.dto.RegisterAccessibilityPostRequest
import club.staircrusher.spring_web.security.app.SccAppAuthentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AccessibilityController(
    private val accessibilityApplicationService: AccessibilityApplicationService,
    private val getImageUploadUrlsUseCase: GetImageUploadUrlsUseCase,
) {
    @PostMapping("/getAccessibility")
    fun getAccessibility(
        @RequestBody request: GetAccessibilityPostRequest,
        authentication: SccAppAuthentication?,
    ): GetAccessibilityPost200Response {
        val result = accessibilityApplicationService.getAccessibility(request.placeId, authentication?.details?.id)
        return GetAccessibilityPost200Response(
            buildingAccessibility = result.buildingAccessibility?.let {
                it.value.toDTO(
                    isUpvoted = result.buildingAccessibilityUpvoteInfo?.isUpvoted ?: false,
                    totalUpvoteCount = result.buildingAccessibilityUpvoteInfo?.totalUpvoteCount ?: 0,
                    registeredUserName = it.userInfo?.nickname,
                )
            },
            placeAccessibility = result.placeAccessibility?.let {
                it.value.toDTO(
                    registeredUserName = it.userInfo?.nickname,
                )
            },
            buildingAccessibilityComments = result.buildingAccessibilityComments.map {
                it.value.toDTO(userInfo = it.userInfo)
            },
            placeAccessibilityComments = result.placeAccessibilityComments.map {
                it.value.toDTO(userInfo = it.userInfo)
            },
            hasOtherPlacesToRegisterInBuilding = result.hasOtherPlacesToRegisterInSameBuilding,
        )
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
        sccAppAuthentication: SccAppAuthentication?,
    ): RegisterAccessibilityPost200Response {
        val userId = sccAppAuthentication?.principal
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
                registeredUserName = result.userInfo?.nickname,
            ),
            buildingAccessibilityComments = listOfNotNull(result.buildingAccessibilityComment).map {
                it.toDTO(userInfo = result.userInfo)
            },
            placeAccessibility = result.placeAccessibility.toDTO(
                registeredUserName = result.userInfo?.nickname,
            ),
            placeAccessibilityComments = listOfNotNull(result.placeAccessibilityComment).map {
                it.toDTO(userInfo = result.userInfo)
            },
            registeredUserOrder = 0, // TODO: 올바르게 채워주기
        )
    }
}
