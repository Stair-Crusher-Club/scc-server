package club.staircrusher.testing.spring_it.mock

import club.staircrusher.user.application.port.out.web.subscription.StibeeSubscriptionService

class MockStibeeSubscriptionService : StibeeSubscriptionService {
    override suspend fun registerSubscriber(email: String, name: String, isMarketingPushAgreed: Boolean): Boolean {
        return true
    }
}
