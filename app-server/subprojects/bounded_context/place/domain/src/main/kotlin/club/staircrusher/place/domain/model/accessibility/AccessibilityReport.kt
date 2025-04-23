package club.staircrusher.place.domain.model.accessibility

import club.staircrusher.stdlib.persistence.jpa.TimeAuditingBaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
class AccessibilityReport(
    @Id
    val id: String,

    @Column(name = "place_id", nullable = false)
    val placeId: String,

    @Column(name = "user_id", nullable = false)
    val userId: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false)
    val reason: AccessibilityReportReason,

    @Column(name = "detail", nullable = true)
    val detail: String?,
) : TimeAuditingBaseEntity() {
    companion object {
        fun create(
            id: String,
            placeId: String,
            userId: String,
            reason: AccessibilityReportReason,
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
