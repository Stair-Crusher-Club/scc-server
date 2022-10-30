package club.staircrusher.quest.domain.model

import club.staircrusher.stdlib.geography.Location

data class ClubQuestTargetBuilding(
    val buildingId: String,
    val name: String,
    val location: Location,
    val places: List<ClubQuestTargetPlace>
)
