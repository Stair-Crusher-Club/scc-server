package club.staircrusher.accessibility.application.port.out.file_management

import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Path

interface FileManagementService {
    fun getFileUploadUrl(filenameExtension: String): UploadUrl
    fun getFileUploadUrl(filename: String, filenameExtension: String): UploadUrl
    fun downloadFile(url: String, destination: Path): File
    fun upload(filename: String, filenameExtension: String, fileBytes: ByteArray): String
    suspend fun uploadThumbnailImage(fileName: String, outputStream: ByteArrayOutputStream): String?
}
