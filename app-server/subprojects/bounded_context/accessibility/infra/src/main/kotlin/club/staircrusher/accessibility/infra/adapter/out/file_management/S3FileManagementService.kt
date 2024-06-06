package club.staircrusher.accessibility.infra.adapter.out.file_management

import club.staircrusher.accessibility.application.port.out.file_management.FileManagementService
import club.staircrusher.accessibility.application.port.out.file_management.UploadUrl
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import kotlinx.coroutines.future.await
import mu.KotlinLogging
import org.springframework.core.io.ResourceLoader
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.ObjectCannedACL
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

@Component
internal class S3FileManagementService(
    private val resourceLoader: ResourceLoader,
    private val properties: S3ImageUploadProperties,
) : FileManagementService {
    private val logger = KotlinLogging.logger {}
    private val s3Presigner = S3Presigner.builder()
        .apply {
            // IRSA가 동작하지 않는 로컬에서 테스트 가능하도록, crendential이 있는 경우에는 credential을 넣고, 아닌 경우에는 넣지 않는다.
            properties.getAwsCredentials()?.let { credentialsProvider { it } }
            region(Region.AP_NORTHEAST_2)
        }
        .build()

    private val s3Client = S3AsyncClient.builder()
        .apply {
            properties.getAwsCredentials()?.let { credentialsProvider { it } }
            region(Region.AP_NORTHEAST_2)
        }
        .build()

    // TODO: 유저별 rate limit 걸기. 위치는 여기가 아니라 application service여야 할 수도 있을 듯.
    override fun getFileUploadUrl(filenameExtension: String): UploadUrl {
        val normalizedFilenameExtension = getNormalizedFileExtension(filenameExtension)
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
            expireAt = SccClock.instant() + presignedUrlExpiryDuration,
        )
    }

    override fun downloadFile(url: String, destination: Path): File {
        val resource = resourceLoader.getResource(url)
        val fileName = resource.filename ?: "tmp"
        val file = File(destination.resolve(fileName).toString())
        resource.inputStream.use { it.copyTo(file.outputStream()) }

        return file
    }

    override suspend fun uploadThumbnailImage(filePath: Path, extension: String, contentType: String): String? {
        val normalizedExtension = getNormalizedFileExtension(extension)
        val objectKey = generateThumbnailObjectKey(normalizedExtension)
        val objectRequest = PutObjectRequest.builder()
            .bucket(properties.thumbnailBucketName)
            .key(objectKey)
            .contentType(contentType)
            .acl(ObjectCannedACL.PUBLIC_READ)
            .build()

        try {
            s3Client.putObject(objectRequest, filePath).await()
            return s3Client
                .utilities()
                .getUrl {
                    it.bucket(properties.thumbnailBucketName)
                    it.key(objectKey)
                }
                .toString()
        } catch (t: Throwable) {
            logger.error(t) { "Failed to upload thumbnail image" }
            return null
        }
    }

    private val objectKeyTimestampPrefixFormat = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
    private fun generateObjectKey(extension: String?): String {
        return buildString {
            append(objectKeyTimestampPrefixFormat.format(SccClock.instant().atOffset(ZoneOffset.UTC)))
            append("_")
            append(generateUUID())
            extension?.let { append(".$extension") }
        }
    }

    // 기존 파일명을 써야 하나?
    private fun generateThumbnailObjectKey(extension: String?): String {
        return buildString {
            append("thumbnail_")
            append(objectKeyTimestampPrefixFormat.format(SccClock.instant().atOffset(ZoneOffset.UTC)))
            append("_")
            append(generateUUID())
            extension?.let { append(".$extension") }
        }
    }

    private fun generateUUID(): String {
        return UUID.randomUUID()
            .toString()
            .replace("-", "")
            .take(16)
            .uppercase()
    }

    private fun getNormalizedFileExtension(filenameExtension: String): String {
        return filenameExtension.replace(Regex("^\\."), "")
    }

    companion object {
        private val presignedUrlExpiryDuration = Duration.ofMinutes(5)
    }
}
