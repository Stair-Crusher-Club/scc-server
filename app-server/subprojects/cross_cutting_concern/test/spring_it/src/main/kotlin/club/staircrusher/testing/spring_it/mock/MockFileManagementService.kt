package club.staircrusher.testing.spring_it.mock

import club.staircrusher.image.application.port.out.file_management.FileManagementService
import club.staircrusher.image.application.port.out.file_management.ImageUploadPurposeType
import club.staircrusher.image.application.port.out.file_management.UploadUrl
import club.staircrusher.stdlib.clock.SccClock
import org.springframework.core.io.ClassPathResource
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Path

class MockFileManagementService : FileManagementService {
    override fun getFileUploadUrl(fileExtension: String, purposeType: ImageUploadPurposeType): UploadUrl {
        return UploadUrl(
            url = "example.$fileExtension",
            expireAt = SccClock.instant().plusSeconds(60L)
        )
    }

    override suspend fun downloadFile(url: String, destination: Path): File {
        return ClassPathResource("example.png").file
    }

    override suspend fun uploadAccessibilityImage(fileName: String, fileBytes: ByteArray): String {
        return fileName
    }

    override suspend fun uploadThumbnailImage(fileName: String, outputStream: ByteArrayOutputStream): String {
        return fileName
    }
}
