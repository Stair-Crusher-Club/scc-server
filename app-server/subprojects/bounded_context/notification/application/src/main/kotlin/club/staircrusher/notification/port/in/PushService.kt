package club.staircrusher.notification.port.`in`

import club.staircrusher.notification.port.out.PushSender
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.application.port.out.persistence.UserProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

@Component
class PushService(
    private val pushSender: PushSender,
    private val userProfileRepository: UserProfileRepository,
    private val transactionManager: TransactionManager,
) {
    data class Notification(
        val title: String?,
        val body: String,
        val link: String?,
        val collapseKey: String?
    )

    fun sendPushNotification(
        userIds: List<String>,
        title: String?,
        body: String,
        deepLink: String?,
    ) = transactionManager.doInTransaction(isReadOnly = true) {
        val userProfiles = userProfileRepository.findAllByUserIdIn(userIds)
        val notifications = userProfiles.mapNotNull { userProfile ->
            userProfile.pushToken ?: return@mapNotNull null
            userProfile.pushToken!! to Notification(
                // just poc for now, but not sure this substitution needs to be placed here
                title = title?.replace("{{nickname}}", userProfile.nickname),
                body = body.replace("{{nickname}}", userProfile.nickname),
                link = deepLink,
                collapseKey = null,
            )
        }

        transactionManager.doAfterCommit {
            CoroutineScope(Dispatchers.IO).launch {
                notifications.map { (t, n) ->
                    async { pushSender.send(t, emptyMap(), n) }
                }.joinAll()
            }
        }
    }
}
