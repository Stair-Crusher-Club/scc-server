package club.staircrusher.accessibility.infra.adapter.out

import club.staircrusher.accessibility.application.port.out.FileManagementService
import club.staircrusher.stdlib.di.annotation.Component
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.util.UUID

@Component
internal class S3FileManagementService(
    private val properties: S3ImageUploadProperties,
) : FileManagementService {
    private val s3Presigner = S3Presigner.builder()
        .credentialsProvider {
            properties.getAwsCredentials()
        }
        .build()

    override fun getFileUploadUrl(filenameExtension: String): FileManagementService.UploadUrl {
        val normalizedFilenameExtension = filenameExtension.replace(Regex("^\\."), "")
        val objectRequest = PutObjectRequest.builder()
            .bucket(properties.bucketName)
            .key(generateObjectKey(normalizedFilenameExtension))
            .contentType(Files.probeContentType(Path.of("dummy.${normalizedFilenameExtension}")))
            .build()

        val s3PresignRequest: PutObjectPresignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(presignedUrlExpiryDuration)
            .putObjectRequest(objectRequest)
            .build()

        val presignedRequest = s3Presigner.presignPutObject(s3PresignRequest)
        return FileManagementService.UploadUrl(
            url = presignedRequest.url().toString(),
            expiryDuration = presignedUrlExpiryDuration,
        )
    }

    private fun generateObjectKey(extension: String?): String {
        return buildString {
            append(UUID.randomUUID().toString())
            extension?.let { append(".$extension") }
        }
    }

    companion object {
        private val presignedUrlExpiryDuration = Duration.ofMinutes(1)
    }
}
