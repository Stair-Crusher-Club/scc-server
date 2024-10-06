package club.staircrusher.notification.adapter.out

import club.staircrusher.notification.port.`in`.PushService
import club.staircrusher.notification.port.out.PushSender
import club.staircrusher.stdlib.di.annotation.Component
import com.google.api.core.ApiFutureToListenableFuture
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.ApnsConfig
import com.google.firebase.messaging.Aps
import com.google.firebase.messaging.ApsAlert
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import kotlinx.coroutines.guava.await
import mu.KotlinLogging
import java.util.UUID

@Component
class PushSender(
    properties: PushSenderProperties,
): PushSender {
    private val logger = KotlinLogging.logger { }
    private val messaging by lazy {
        val firebaseApp = FirebaseApp.initializeApp(
            FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(properties.credential.byteInputStream()))
                .build(),
            UUID.randomUUID().toString(),
        )

        FirebaseMessaging.getInstance(firebaseApp)
    }

    public override suspend fun send(
        pushToken: String,
        customData: Map<String, String>,
        notification: PushService.Notification,
    ): Boolean {
        return send(
            pushToken,
            getApnsConfig(customData, notification),
            getAndroidConfig(customData, notification),
        )
    }

    private suspend fun send(
        pushToken: String,
        apnsConfig: ApnsConfig,
        androidConfig: AndroidConfig,
    ): Boolean {
        val future = messaging.sendAsync(
            Message.builder().apply {
                setToken(pushToken)
                setApnsConfig(apnsConfig)
                setAndroidConfig(androidConfig)
            }.build()
        )

        return try {
            ApiFutureToListenableFuture(future).await()
            true
        } catch (e: Throwable) {
            logger.error(e) { "Failed to send push notification" }
            return false
        }
    }

    private fun getAndroidConfig(
        customData: Map<String, String>,
        notification: PushService.Notification,
    ): AndroidConfig {
        return AndroidConfig.builder().apply {
            notification.collapseKey?.let { setCollapseKey(it) }
            putAllData(customData)
            putData(DEEPLINK_CUSTOM_DATA_KEY, notification.link)
            setPriority(AndroidConfig.Priority.HIGH)
        }.build()
    }

    private fun getApnsConfig(
        customData: Map<String, String>,
        notification: PushService.Notification,
    ): ApnsConfig {
        val apsAlert = ApsAlert.builder().apply {
            notification.title?.let { setTitle(it) }
            setBody(notification.body)
        }.build()
        val aps = Aps.builder().setAlert(apsAlert).build()

        return ApnsConfig.builder().apply {
            putAllCustomData(customData)
            putCustomData(DEEPLINK_CUSTOM_DATA_KEY, notification.link)
            setAps(aps)
            notification.collapseKey?.let { putHeader("apns-collapse-id", it) }
        }.build()
    }

    companion object {
        private const val DEEPLINK_CUSTOM_DATA_KEY = "_d"
    }
}
