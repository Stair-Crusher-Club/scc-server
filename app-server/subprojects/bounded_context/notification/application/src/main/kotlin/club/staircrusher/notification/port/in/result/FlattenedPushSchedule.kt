package club.staircrusher.notification.port.`in`.result

import java.time.Instant

class FlattenedPushSchedule(
    val groupId: String,
    val scheduledAt: Instant?,
    val sentAt: Instant?,
    val title: String?,
    val body: String,
    val deepLink: String?,
    val userIds: List<String>,
    val createdAt: Instant,
)
