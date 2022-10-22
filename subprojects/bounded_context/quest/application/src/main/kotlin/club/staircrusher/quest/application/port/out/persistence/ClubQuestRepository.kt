package club.staircrusher.quest.application.port.out.persistence

import club.staircrusher.quest.domain.model.ClubQuest

interface ClubQuestRepository {
    fun save(clubQuest: ClubQuest)
}
