package club.staircrusher.spring_web.mock

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.user.application.port.out.web.subscription.StibeeSubscriptionService
import org.springframework.context.annotation.Primary

@Primary
@Component
class MockStibeeSubscriptionService : StibeeSubscriptionService {
    override suspend fun registerSubscriber(email: String, name: String, isMarketingPushAgreed: Boolean): Boolean {
        return true
    }
}
