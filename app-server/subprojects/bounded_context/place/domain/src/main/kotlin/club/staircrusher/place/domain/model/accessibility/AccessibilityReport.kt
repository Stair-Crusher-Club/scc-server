package club.staircrusher.place.domain.model.accessibility

import club.staircrusher.stdlib.persistence.jpa.TimeAuditingBaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "accessibility_report")
class AccessibilityReport(
    @Id
    val id: String,

    @Column(name = "place_id", nullable = false)
    val placeId: String,

    @Column(name = "user_id", nullable = false)
    val userId: String,

    @Column(name = "reason", nullable = true)
    val reason: String?,

    @Column(name = "detail", nullable = true)
    val detail: String?,
) : TimeAuditingBaseEntity() {
    companion object {
        fun create(
            id: String,
            placeId: String,
            userId: String,
            reason: String?,
            detail: String?,
        ): AccessibilityReport {
            return AccessibilityReport(
                id = id,
                placeId = placeId,
                userId = userId,
                reason = reason,
                detail = detail,
            )
        }
    }
}
