package club.staircrusher.notification.port.out.persistence

import club.staircrusher.notification.domain.model.PushNotificationSchedule
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.Instant

interface PushNotificationScheduleRepository : CrudRepository<PushNotificationSchedule, String> {
    @Query("""
        SELECT s
        FROM PushNotificationSchedule s
        WHERE
            (
                (s.createdAt = :cursorCreatedAt AND s.groupId < :cursorId)
                OR (s.createdAt < :cursorCreatedAt)
            )
        ORDER BY s.createdAt DESC, s.groupId DESC
    """)
    fun findCursored(
        cursorCreatedAt: Instant,
        cursorId: String,
        pageable: Pageable
    ): Page<PushNotificationSchedule>

    fun findByGroupId(groupId: String): List<PushNotificationSchedule>

    fun findAllByScheduledAtBeforeAndSentAtIsNull(scheduledAt: Instant): List<PushNotificationSchedule>

    fun findAllByCreatedAtAfterOrderByCreatedAtDesc(createdAt: Instant): List<PushNotificationSchedule>

    fun deleteAllByGroupId(groupId: String)
}
