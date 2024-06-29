package club.staircrusher.accessibility.application.port.out.file_management

import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Path

interface FileManagementService {
    fun getFileUploadUrl(fileExtension: String): UploadUrl
    fun getFileUploadUrl(fileName: String, fileExtension: String): UploadUrl
    fun downloadFile(url: String, destination: Path): File
    suspend fun uploadImage(fileName: String, fileBytes: ByteArray): String?
    suspend fun uploadThumbnailImage(fileName: String, outputStream: ByteArrayOutputStream): String?
}
