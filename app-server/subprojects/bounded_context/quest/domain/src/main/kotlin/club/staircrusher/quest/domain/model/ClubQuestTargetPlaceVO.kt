package club.staircrusher.quest.domain.model

import club.staircrusher.stdlib.geography.Location

// FIXME: rename to DryRunnedClubQuestTargetPlace after DB column delete
@Deprecated(
    "성능 이슈로 인해 별도의 entity로 분리한다.",
    replaceWith = ReplaceWith("ClubQuestTargetBuilding"),
)
data class ClubQuestTargetPlaceVO(
    val buildingId: String,
    val placeId: String,
    val name: String,
    val location: Location,
    val isClosed: Boolean,
    val isNotAccessible: Boolean,
)
