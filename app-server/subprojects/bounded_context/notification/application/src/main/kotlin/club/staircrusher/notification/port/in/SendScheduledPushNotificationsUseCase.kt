package club.staircrusher.notification.port.`in`

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager
import mu.KotlinLogging
import java.util.concurrent.Executors

@Component
class SendScheduledPushNotificationsUseCase(
    private val transactionManager: TransactionManager,
    private val pushService: PushService,
    private val pushScheduleService: PushScheduleService,
) {
    private val logger = KotlinLogging.logger {}
    private val executor = Executors.newSingleThreadExecutor()

    fun handle() {
        executor.submit {
            doHandle()
        }
    }

    private fun doHandle() {
        val targetSchedules = transactionManager.doInTransaction(isReadOnly = true) {
            pushScheduleService.getOutstandingSchedules()
        }
        if (targetSchedules.isEmpty()) return

        logger.info { "Sending ${targetSchedules.size} scheduled push notifications" }
        val failedScheduleIds = targetSchedules.mapNotNull { schedule ->
            try {
                pushService.sendPushNotification(
                    userIds = schedule.userIds,
                    title = schedule.title,
                    body = schedule.body,
                    deepLink = schedule.deepLink,
                    collapseKey = schedule.id,
                )

                transactionManager.doInTransaction {
                    pushScheduleService.updateSentAt(schedule.id, SccClock.instant())
                }
                null
            } catch (e: Exception) {
                logger.error(e) { "Error while sending push notification for schedule ${schedule.id}" }
                schedule.id
            }
        }

        if (failedScheduleIds.isNotEmpty()) {
            logger.info { "Failed to send ${failedScheduleIds.size} out of ${targetSchedules.size} scheduled push notifications (${failedScheduleIds.joinToString()})" }
        } else {
            logger.info { "Successfully sent ${targetSchedules.size} scheduled push notifications" }
        }
    }
}
