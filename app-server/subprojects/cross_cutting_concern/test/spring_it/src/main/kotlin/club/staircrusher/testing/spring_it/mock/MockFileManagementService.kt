package club.staircrusher.testing.spring_it.mock

import club.staircrusher.accessibility.application.port.out.file_management.FileManagementService
import club.staircrusher.accessibility.application.port.out.file_management.UploadUrl
import club.staircrusher.stdlib.clock.SccClock
import org.springframework.core.io.ClassPathResource
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Path

class MockFileManagementService : FileManagementService {
    override fun getFileUploadUrl(fileExtension: String): UploadUrl {
        return getFileUploadUrl("example", fileExtension)
    }

    override fun getFileUploadUrl(fileName: String, fileExtension: String): UploadUrl {
        return UploadUrl(
            url = "$fileName.$fileExtension",
            expireAt = SccClock.instant().plusSeconds(60L)
        )
    }

    override fun downloadFile(url: String, destination: Path): File {
        return ClassPathResource("example.png").file
    }

    override suspend fun uploadImage(fileName: String, fileBytes: ByteArray): String {
        return fileName
    }

    override suspend fun uploadThumbnailImage(fileName: String, outputStream: ByteArrayOutputStream): String {
        return fileName
    }
}
