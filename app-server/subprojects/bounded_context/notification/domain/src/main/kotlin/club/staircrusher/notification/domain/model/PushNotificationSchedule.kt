package club.staircrusher.notification.domain.model

import club.staircrusher.stdlib.clock.SccClock
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

    val groupId: String,

    var scheduledAt: Instant,

    var title: String?,

    var body: String,

    var deepLink: String?,

    @Convert(converter = StringListToTextAttributeConverter::class)
    var userIds: List<String>
) : TimeAuditingBaseEntity() {
    init {
        check(scheduledAt.isAfter(SccClock.instant()))
    }

    private var sentAt: Instant? = null

    fun isSent(): Boolean {
        return sentAt != null
    }

    fun updateSentAt(sentAt: Instant) {
        this.sentAt = sentAt
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

    override fun toString(): String {
        return "PushNotificationSchedule(id='$id', groupId='$groupId', scheduledAt=$scheduledAt, sentAt=$sentAt, title=$title, body='$body', deepLink=$deepLink, userIds=$userIds, createdAt=$createdAt, updatedAt=$updatedAt)"
    }
}
