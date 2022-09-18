package club.staircrusher.quest.domain.vo

import club.staircrusher.stdlib.geography.Location

data class ClubQuestTargetPlace(
    val name: String,
    val location: Location,
    val placeId: String,
)
