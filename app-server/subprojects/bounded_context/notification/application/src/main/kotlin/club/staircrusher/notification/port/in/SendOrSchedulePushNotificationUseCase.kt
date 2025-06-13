package club.staircrusher.notification.port.`in`

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager
import java.time.Instant
import java.util.concurrent.Executors

@Component
class SendOrSchedulePushNotificationUseCase(
    private val transactionManager: TransactionManager,
    private val pushService: PushService,
    private val pushScheduleService: PushScheduleService,
) {
    private val executor = Executors.newSingleThreadExecutor()

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
        )
    }

    private fun sendPushNotificationImmediately(
        title: String?,
        body: String,
        deepLink: String?,
        targetUserIds: List<String>,
    ) {
        executor.submit {
            pushService.sendPushNotification(
                userIds = targetUserIds,
                title = title,
                body = body,
                deepLink = deepLink,
                collapseKey = null,
            )
        }
    }
}
