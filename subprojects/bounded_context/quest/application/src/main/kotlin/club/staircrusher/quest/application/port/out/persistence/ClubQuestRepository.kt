package club.staircrusher.quest.application.port.out.persistence

import club.staircrusher.quest.domain.model.ClubQuest
import club.staircrusher.stdlib.domain.repository.EntityRepository

interface ClubQuestRepository : EntityRepository<ClubQuest, String> {
    fun findAllOrderByCreatedAtDesc(): List<ClubQuest>

    fun remove(clubQuestId: String)
}
