package club.staircrusher.accessibility.application.port.out.file_management

import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Path

interface FileManagementService {
    fun getFileUploadUrl(filenameExtension: String): UploadUrl
    fun downloadFile(url: String, destination: Path): File
    suspend fun uploadThumbnailImage(fileName: String, outputStream: ByteArrayOutputStream): String?
}
