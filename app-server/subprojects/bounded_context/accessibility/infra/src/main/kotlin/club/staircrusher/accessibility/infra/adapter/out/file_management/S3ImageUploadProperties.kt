package club.staircrusher.accessibility.infra.adapter.out.file_management

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentials

@ConfigurationProperties("scc.s3.image-upload")
internal data class S3ImageUploadProperties @ConstructorBinding constructor(
    val bucketName: String,
    val thumbnailBucketName: String,
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
