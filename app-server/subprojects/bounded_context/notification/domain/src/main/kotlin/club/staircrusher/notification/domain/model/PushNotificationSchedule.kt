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

    /**
     * 스케줄 생성시 user id 를 1000개씩 갖도록 청킹
     * 그렇게 한번에 생성됐지만 row 가 여러개 있는 경우를 묶기 위한 id
     */
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
