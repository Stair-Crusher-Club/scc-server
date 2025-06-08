package club.staircrusher.notification.domain.model

import club.staircrusher.stdlib.persistence.jpa.StringListToTextAttributeConverter
import club.staircrusher.stdlib.persistence.jpa.TimeAuditingBaseEntity
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.Instant

@Entity
class PushNotificationSchedule(
    @Id
    val id: String,

    var scheduledAt: Instant,

    var sentAt: Instant?,

    var title: String?,

    var body: String,

    var deepLink: String?,

    @Convert(converter = StringListToTextAttributeConverter::class)
    var userIds: List<String>,

    ) : TimeAuditingBaseEntity() {
    fun isSent(): Boolean {
        return sentAt != null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PushNotificationSchedule

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
