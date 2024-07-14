package club.staircrusher.user.application.port.out.web.subscription

interface StibeeSubscriptionService {
    suspend fun registerSubscriber(email: String, name: String, isMarketingPushAgreed: Boolean): Boolean
}
