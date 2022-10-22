package club.staircrusher.accessibility.infra.adapter.out.file_management

import club.staircrusher.accessibility.application.port.out.file_management.FileManagementService
import club.staircrusher.accessibility.application.port.out.file_management.UploadUrl
import club.staircrusher.stdlib.di.annotation.Component
import software.amazon.awssdk.services.s3.model.ObjectCannedACL
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.nio.file.Files
import java.nio.file.Path
import java.time.Clock
import java.time.Duration
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

@Component
internal class S3FileManagementService(
    private val clock: Clock,
    private val properties: S3ImageUploadProperties,
) : FileManagementService {
    private val s3Presigner = S3Presigner.builder()
        .apply {
            // IRSA가 동작하지 않는 로컬에서 테스트 가능하도록, crendential이 있는 경우에는 credential을 넣고, 아닌 경우에는 넣지 않는다.
            properties.getAwsCredentials()?.let { credentialsProvider { it } }
        }
        .build()

    // TODO: 유저별 rate limit 걸기. 위치는 여기가 아니라 application service여야 할 수도 있을 듯.
    override fun getFileUploadUrl(filenameExtension: String): UploadUrl {
        val normalizedFilenameExtension = filenameExtension.replace(Regex("^\\."), "")
        val objectRequest = PutObjectRequest.builder()
            .bucket(properties.bucketName)
            .key(generateObjectKey(normalizedFilenameExtension))
            .contentType(Files.probeContentType(Path.of("dummy.${normalizedFilenameExtension}")))
            .acl(ObjectCannedACL.PUBLIC_READ)
            .build()

        val s3PresignRequest: PutObjectPresignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(presignedUrlExpiryDuration)
            .putObjectRequest(objectRequest)
            .build()

        val presignedRequest = s3Presigner.presignPutObject(s3PresignRequest)
        return UploadUrl(
            url = presignedRequest.url().toString(),
            expireAt = clock.instant() + presignedUrlExpiryDuration,
        )
    }

    private val objectKeyTimestampPrefixFormat = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
    private fun generateObjectKey(extension: String?): String {
        return buildString {
            append(objectKeyTimestampPrefixFormat.format(clock.instant().atOffset(ZoneOffset.UTC)))
            append("_")
            @Suppress("MagicNumber")
            UUID.randomUUID()
                .toString()
                .replace("-", "")
                .take(16)
                .uppercase()
                .let { append(it) }
            extension?.let { append(".$extension") }
        }
    }

    companion object {
        private val presignedUrlExpiryDuration = Duration.ofMinutes(5)
    }
}
