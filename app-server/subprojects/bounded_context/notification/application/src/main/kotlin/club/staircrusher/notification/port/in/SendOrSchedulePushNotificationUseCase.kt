package club.staircrusher.notification.port.`in`

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager
import java.time.Instant

@Component
class SendOrSchedulePushNotificationUseCase(
    private val transactionManager: TransactionManager,
    private val pushService: PushService,
    private val pushScheduleService: PushScheduleService,
) {
    fun handle(
        scheduledAt: Instant?,
        title: String?,
        body: String,
        deepLink: String?,
        targetUserIds: List<String>,
    ) {
        if (scheduledAt == null) {
            sendPushNotificationImmediately(title, body, deepLink, targetUserIds)
        } else {
            createPushNotificationSchedule(scheduledAt, title, body, deepLink, targetUserIds)
        }
    }

    private fun createPushNotificationSchedule(
        scheduledAt: Instant,
        title: String?,
        body: String,
        deepLink: String?,
        targetUserIds: List<String>,
    ) = transactionManager.doInTransaction {
        pushScheduleService.create(
            scheduledAt = scheduledAt,
            title = title,
            body = body,
            deepLink = deepLink,
            userIds = targetUserIds,
            sentAt = null,
        )
    }

    private fun sendPushNotificationImmediately(
        title: String?,
        body: String,
        deepLink: String?,
        targetUserIds: List<String>,
    ) = transactionManager.doInTransaction {
        val now = SccClock.instant()
        pushScheduleService.create(
            scheduledAt = now,
            title = title,
            body = body,
            deepLink = deepLink,
            userIds = targetUserIds,
            sentAt = now,
        )

        transactionManager.doAfterCommit {
            pushService.sendPushNotification(
                userIds = targetUserIds,
                title = title,
                body = body,
                deepLink = deepLink,
            )
        }
    }
}
