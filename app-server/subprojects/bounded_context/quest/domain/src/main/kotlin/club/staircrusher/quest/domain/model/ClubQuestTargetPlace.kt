package club.staircrusher.quest.domain.model

import club.staircrusher.stdlib.geography.Location

data class ClubQuestTargetPlace(
    val name: String,
    val location: Location,
    val buildingId: String,
    val placeId: String,
    val isClosed: Boolean,
    val isNotAccessible: Boolean,
)
