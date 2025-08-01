package club.staircrusher.infra.message_queue

import org.springframework.boot.context.properties.ConfigurationProperties
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentials

@ConfigurationProperties("scc.sqs")
data class SqsProperties(
    val commonQueueName: String,
    val accessKey: String?,
    val secretKey: String?,
) {
    fun getAwsCredentials(): AwsCredentials? {
        return if (accessKey != null && secretKey != null) {
            AwsBasicCredentials.create(accessKey, secretKey)
        } else {
            null
        }
    }
}
