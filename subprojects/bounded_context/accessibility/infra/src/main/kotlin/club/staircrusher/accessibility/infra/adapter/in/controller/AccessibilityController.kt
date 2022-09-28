package club.staircrusher.accessibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.application.AccessibilityApplicationService
import club.staircrusher.accessibility.infra.adapter.`in`.converter.toDTO
import club.staircrusher.api.spec.dto.GetAccessibilityPost200Response
import club.staircrusher.api.spec.dto.GetAccessibilityPostRequest
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
}
