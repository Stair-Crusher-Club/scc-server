package club.staircrusher.quest.domain.repository

import club.staircrusher.quest.domain.entity.ClubQuest

interface ClubQuestRepository {
    fun save(clubQuest: ClubQuest)
}
