package club.staircrusher.image.infra.adapter.out.file_management

import club.staircrusher.image.application.port.out.file_management.FileManagementService
import club.staircrusher.image.application.port.out.file_management.ImageUploadPurposeType
import club.staircrusher.image.application.port.out.file_management.UploadUrl
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import kotlinx.coroutines.future.await
import mu.KotlinLogging
import org.springframework.core.io.ResourceLoader
import software.amazon.awssdk.core.async.AsyncRequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.ObjectCannedACL
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.io.ByteArrayOutputStream
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
    override fun getFileUploadUrl(fileExtension: String, purposeType: ImageUploadPurposeType): UploadUrl {
        val normalizedFilenameExtension = getNormalizedFileExtension(fileExtension)
        val objectRequest = PutObjectRequest.builder()
            .bucket(purposeType.bucketName)
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

    override suspend fun downloadFile(url: String, destination: Path): File {
        val resource = resourceLoader.getResource(url)
        val fileName = resource.filename ?: "tmp"
        val file = File(destination.resolve(fileName).toString())
        resource.inputStream.use { it.copyTo(file.outputStream()) }

        return file
    }

    override suspend fun uploadAccessibilityImage(fileName: String, fileBytes: ByteArray): String? {
        try {
            return upload(properties.bucketName, fileName, fileBytes)
        } catch (t: Throwable) {
            logger.error(t) { "Failed to upload image: $fileName" }
            return null
        }
    }

    override suspend fun uploadThumbnailImage(fileName: String, outputStream: ByteArrayOutputStream): String? {
        try {
            return upload(properties.thumbnailBucketName, fileName, outputStream.toByteArray())
        } catch (t: Throwable) {
            logger.error(t) { "Failed to upload thumbnail image: $fileName" }
            return null
        }
    }

    private suspend fun upload(bucketName: String, fileName: String, fileBytes: ByteArray): String {
        val objectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(fileName)
            .acl(ObjectCannedACL.PUBLIC_READ)
            .build()
        s3Client.putObject(objectRequest, AsyncRequestBody.fromBytes(fileBytes)).await()
        return s3Client
            .utilities()
            .getUrl {
                it.bucket(bucketName)
                it.key(fileName)
            }
            .toString()
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

    private val ImageUploadPurposeType.bucketName: String
        get() = when (this) {
            ImageUploadPurposeType.ACCESSIBILITY -> properties.bucketName
            ImageUploadPurposeType.ACCESSIBILITY_THUMBNAIL -> properties.thumbnailBucketName
            ImageUploadPurposeType.BANNER -> properties.bannerBucketName
        }

    companion object {
        private val presignedUrlExpiryDuration = Duration.ofMinutes(5)
    }
}
