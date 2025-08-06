package club.staircrusher.place.application.port.`in`.accessibility.result

import club.staircrusher.challenge.domain.model.ChallengeCrusherGroup
import club.staircrusher.place.domain.model.accessibility.BuildingAccessibility
import club.staircrusher.place.domain.model.accessibility.BuildingAccessibilityComment
import club.staircrusher.place.domain.model.accessibility.PlaceAccessibility
import club.staircrusher.place.domain.model.accessibility.PlaceAccessibilityComment

data class GetAccessibilityResult(
    val buildingAccessibility: WithUserInfo<BuildingAccessibility>?,
    val buildingAccessibilityUpvoteInfo: BuildingAccessibilityUpvoteInfo?,
    val buildingAccessibilityComments: List<WithUserInfo<BuildingAccessibilityComment>>,
    val buildingAccessibilityChallengeCrusherGroup: ChallengeCrusherGroup?,
    val placeAccessibility: WithUserInfo<PlaceAccessibility>?,
    val placeAccessibilityComments: List<WithUserInfo<PlaceAccessibilityComment>>,
    val placeAccessibilityChallengeCrusherGroup: ChallengeCrusherGroup?,
    val hasOtherPlacesToRegisterInSameBuilding: Boolean,
    val isFavoritePlace: Boolean,
    val totalFavoriteCount: Long
) {
    data class BuildingAccessibilityUpvoteInfo(
        val isUpvoted: Boolean,
        val totalUpvoteCount: Int,
    )
}
