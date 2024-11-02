package club.staircrusher.image.application.port.out.file_management

import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Path

interface FileManagementService {
    fun getFileUploadUrl(fileExtension: String, purposeType: ImageUploadPurposeType): UploadUrl
    fun downloadFile(url: String, destination: Path): File
    suspend fun uploadAccessibilityImage(fileName: String, fileBytes: ByteArray): String?
    suspend fun uploadThumbnailImage(fileName: String, outputStream: ByteArrayOutputStream): String?
}
