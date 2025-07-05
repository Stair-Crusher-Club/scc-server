package club.staircrusher.place.infra.adapter.out.web

import org.springframework.boot.context.properties.ConfigurationProperties
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentials

@ConfigurationProperties("scc.rekognition")
internal data class RekognitionProperties(
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
