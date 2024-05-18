package club.staircrusher.quest.domain.model

import club.staircrusher.stdlib.geography.Location

data class DryRunnedClubQuestTargetPlace(
    val buildingId: String,
    val placeId: String,
    val name: String,
    val location: Location,
)
