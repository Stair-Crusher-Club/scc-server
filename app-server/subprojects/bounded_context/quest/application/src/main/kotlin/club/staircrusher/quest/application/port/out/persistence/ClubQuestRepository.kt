package club.staircrusher.quest.application.port.out.persistence

import club.staircrusher.quest.domain.model.ClubQuest
import club.staircrusher.quest.domain.model.ClubQuestSummary
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.Instant

interface ClubQuestRepository : CrudRepository<ClubQuest, String> {
    @Query("""
        SELECT q
        FROM ClubQuest q
        WHERE
            (
                (q.createdAt = :cursorCreatedAt AND q.id < :cursorId)
                OR (q.createdAt < :cursorCreatedAt)
            )
        ORDER BY q.createdAt DESC, q.id DESC
    """)
    fun findCursoredSummaries(
        cursorCreatedAt: Instant,
        cursorId: String,
        pageable: Pageable,
    ): Page<ClubQuestSummary>
}
