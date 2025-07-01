package club.staircrusher.infra.message_queue

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsAsyncClient

@Configuration(proxyBeanMethods = false)
internal class AwsSqsConfiguration(
    private val sqsProperties: SqsProperties,
) {
    @Bean
    fun sqsAsyncClient(): SqsAsyncClient {
        return SqsAsyncClient.builder()
            .apply {
                sqsProperties.getAwsCredentials()?.let { credentialsProvider { it } }
                region(Region.AP_NORTHEAST_2)
            }
            .build()
    }
}
