package club.staircrusher.user.infra.adapter.out.web.subscription.client

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.user.application.port.out.web.subscription.StibeeSubscriptionService
import club.staircrusher.user.infra.adapter.out.web.subscription.StibeeProperties
import kotlinx.coroutines.reactive.awaitFirst

@Component
internal class StibeeSubscriptionServiceImpl(
    private val stibeeProperties: StibeeProperties,
    private val stibeeApiClient: StibeeApiClient,
) : StibeeSubscriptionService {
    override suspend fun registerSubscriber(email: String, name: String, isMarketingPushAgreed: Boolean): Boolean {
        val responseDto = stibeeApiClient.registerSubscriber(
            listId = stibeeProperties.listId,
            body = StibeeApiClient.RegisterSubscriberRequestDto(
                subscribers = listOf(
                    StibeeApiClient.RegisterSubscriberRequestDto.Subscriber(
                        email = email,
                        name = name,
                        isMarketingPushAgreed = isMarketingPushAgreed,
                    ),
                ),
                confirmEmailYN = "N",
            ),
        ).awaitFirst()

        return responseDto.isOk
    }
}
