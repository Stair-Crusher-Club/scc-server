package club.staircrusher.user.infra.adapter.out.web.subscription.client

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.user.application.port.out.web.subscription.StibeeSubscriptionService
import club.staircrusher.user.infra.adapter.out.web.subscription.StibeeProperties
import kotlinx.coroutines.reactive.awaitFirst
import mu.KotlinLogging

@Component
internal class StibeeSubscriptionServiceImpl(
    private val stibeeProperties: StibeeProperties,
    private val stibeeApiClient: StibeeApiClient,
) : StibeeSubscriptionService {
    private val logger = KotlinLogging.logger {}

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

        logger.info("Stibee subscription response: $responseDto")

        return responseDto.isOk
    }
}
