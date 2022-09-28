package club.staircrusher.accessibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.application.AccessibilityApplicationService
import club.staircrusher.accessibility.domain.service.BuildingAccessibilityCommentService
import club.staircrusher.accessibility.domain.service.PlaceAccessibilityCommentService
import club.staircrusher.accessibility.infra.adapter.`in`.converter.toDTO
import club.staircrusher.accessibility.infra.adapter.`in`.converter.toModel
import club.staircrusher.api.spec.dto.GetAccessibilityPost200Response
import club.staircrusher.api.spec.dto.GetAccessibilityPostRequest
import club.staircrusher.api.spec.dto.RegisterAccessibilityPost200Response
import club.staircrusher.api.spec.dto.RegisterAccessibilityPostRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AccessibilityController(
    private val accessibilityApplicationService: AccessibilityApplicationService,
) {
    @PostMapping("/getAccessibility")
    fun getAccessibility(@RequestBody request: GetAccessibilityPostRequest): GetAccessibilityPost200Response {
        val result = accessibilityApplicationService.getAccessibility(request.placeId)
        // TODO: upvote 정보 및 유저 정보 제대로 채우기
        return GetAccessibilityPost200Response(
            buildingAccessibility = result.buildingAccessibility?.toDTO(
                isUpvoted = false,
                totalUpvoteCount = 0,
                registeredUserName = null,
            ),
            placeAccessibility = result.placeAccessibility?.toDTO(
                registeredUserName = null,
            ),
            buildingAccessibilityComments = result.buildingAccessibilityComments.map { it.toDTO(user = null) },
            placeAccessibilityComments = result.placeAccessibilityComments.map { it.toDTO(user = null) },
            hasOtherPlacesToRegisterInBuilding = result.hasOtherPlacesToRegisterInSameBuilding,
        )
    }

    @PostMapping("/registerAccessibility")
    fun registerAccessibility(@RequestBody request: RegisterAccessibilityPostRequest): RegisterAccessibilityPost200Response {
        // TODO: Spring Security를 통해 access token의 userId 추출해서 사용해야 함.
        // TODO: upvote 정보 및 유저 정보 제대로 채우기
        val result = accessibilityApplicationService.register(
            createBuildingAccessibilityParams = request.buildingAccessibilityParams?.toModel(userId = ""),
            createBuildingAccessibilityCommentParams = request.buildingAccessibilityParams?.comment?.let {
                BuildingAccessibilityCommentService.CreateParams(
                    buildingId = request.buildingAccessibilityParams!!.buildingId,
                    userId = null,
                    comment = it,
                )
            },
            createPlaceAccessibilityParams = request.placeAccessibilityParams.toModel(userId = ""),
            createPlaceAccessibilityCommentParams = request.placeAccessibilityParams.comment?.let {
                PlaceAccessibilityCommentService.CreateParams(
                    placeId = request.placeAccessibilityParams.placeId,
                    userId = null,
                    comment = it,
                )
            },
        )
        return RegisterAccessibilityPost200Response(
            buildingAccessibility = result.buildingAccessibility?.toDTO(
                isUpvoted = false,
                totalUpvoteCount = 0,
                registeredUserName = null,
            ),
            buildingAccessibilityComments = listOfNotNull(result.buildingAccessibilityComment).map {
                it.toDTO(user = null)
            },
            placeAccessibility = result.placeAccessibility.toDTO(
                registeredUserName = null,
            ),
            placeAccessibilityComments = listOfNotNull(result.placeAccessibilityComment).map {
                it.toDTO(user = null)
            },
            registeredUserOrder = 0,
        )
    }
}
