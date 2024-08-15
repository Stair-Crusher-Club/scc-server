package club.staircrusher.accessibility.application.port.`in`.result

import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.BuildingAccessibilityComment
import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.accessibility.domain.model.PlaceAccessibilityComment
import club.staircrusher.challenge.domain.model.ChallengeCrusherGroup

data class GetAccessibilityResult(
    val buildingAccessibility: WithUserInfo<BuildingAccessibility>?,
    val buildingAccessibilityUpvoteInfo: BuildingAccessibilityUpvoteInfo?,
    val buildingAccessibilityComments: List<WithUserInfo<BuildingAccessibilityComment>>,
    val buildingAccessibilityChallengeCrusherGroup: ChallengeCrusherGroup?,
    val placeAccessibility: WithUserInfo<PlaceAccessibility>?,
    val placeAccessibilityComments: List<WithUserInfo<PlaceAccessibilityComment>>,
    val placeAccessibilityChallengeCrusherGroup: ChallengeCrusherGroup?,
    val hasOtherPlacesToRegisterInSameBuilding: Boolean,
    val isLastPlaceAccessibilityInBuilding: Boolean,
    val isFavoritePlace: Boolean,
    val totalFavoriteCount: Long
) {
    data class BuildingAccessibilityUpvoteInfo(
        val isUpvoted: Boolean,
        val totalUpvoteCount: Int,
    )
}
