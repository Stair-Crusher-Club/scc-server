package club.staircrusher.quest.application.port.out.persistence

import club.staircrusher.quest.domain.model.ClubQuest
import club.staircrusher.quest.domain.model.ClubQuestSummary
import club.staircrusher.stdlib.domain.repository.EntityRepository
import java.time.Instant

interface ClubQuestRepository : EntityRepository<ClubQuest, String> {
    fun findAllOrderByCreatedAtDesc(): List<ClubQuest>
    fun findCursoredSummariesOrderByCreatedAtDesc(
        cursorCreatedAt: Instant,
        cursorId: String,
        limit: Int
    ): List<ClubQuestSummary>

    fun remove(clubQuestId: String)
}
