package club.staircrusher.quest.domain.model

import club.staircrusher.stdlib.geography.Location

// FIXME: rename to DryRunnedClubQuestTargetBuilding after DB column delete
@Deprecated(
    "성능 이슈로 인해 별도의 entity로 분리한다.",
    replaceWith = ReplaceWith("ClubQuestTargetBuilding"),
)
data class DryRunnedClubQuestTargetBuilding(
    val buildingId: String,
    val name: String,
    val location: Location,
    val places: List<DryRunnedClubQuestTargetPlace>,
)
