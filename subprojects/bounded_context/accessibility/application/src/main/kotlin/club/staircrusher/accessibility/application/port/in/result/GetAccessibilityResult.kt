package club.staircrusher.accessibility.application.port.`in`.result

import club.staircrusher.accessibility.application.port.`in`.AccessibilityApplicationService
import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.BuildingAccessibilityComment
import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.accessibility.domain.model.PlaceAccessibilityComment

data class GetAccessibilityResult(
    val buildingAccessibility: AccessibilityApplicationService.WithUserInfo<BuildingAccessibility>?,
    val buildingAccessibilityUpvoteInfo: BuildingAccessibilityUpvoteInfo?,
    val buildingAccessibilityComments: List<AccessibilityApplicationService.WithUserInfo<BuildingAccessibilityComment>>,
    val placeAccessibility: AccessibilityApplicationService.WithUserInfo<PlaceAccessibility>?,
    val placeAccessibilityComments: List<AccessibilityApplicationService.WithUserInfo<PlaceAccessibilityComment>>,
    val hasOtherPlacesToRegisterInSameBuilding: Boolean,
    val isLastPlaceAccessibilityInBuilding: Boolean,
) {
    data class BuildingAccessibilityUpvoteInfo(
        val isUpvoted: Boolean,
        val totalUpvoteCount: Int,
    )
}
