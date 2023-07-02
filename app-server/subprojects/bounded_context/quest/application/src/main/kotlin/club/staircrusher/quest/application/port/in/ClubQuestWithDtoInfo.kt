package club.staircrusher.quest.application.port.`in`

import club.staircrusher.quest.domain.model.ClubQuest

data class ClubQuestWithDtoInfo(
    val quest: ClubQuest,
    val conqueredPlaceIds: Set<String>,
)
