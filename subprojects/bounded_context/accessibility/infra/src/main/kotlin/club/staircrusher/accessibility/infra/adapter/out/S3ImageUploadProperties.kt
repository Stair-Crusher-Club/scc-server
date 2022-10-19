package club.staircrusher.accessibility.infra.adapter.out

import club.staircrusher.stdlib.di.annotation.Component
import org.springframework.boot.context.properties.ConfigurationProperties
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentials

@Component
@ConfigurationProperties("scc.s3.image-upload")
internal class S3ImageUploadProperties {
    var bucketName: String = ""
    var accessKey: String = ""
    var secretKey: String = ""

    fun getAwsCredentials(): AwsCredentials {
        return AwsBasicCredentials.create(accessKey, secretKey)
    }
}
