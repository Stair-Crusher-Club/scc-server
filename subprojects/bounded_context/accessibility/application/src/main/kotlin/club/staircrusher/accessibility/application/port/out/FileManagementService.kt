package club.staircrusher.accessibility.application.port.out

import java.time.Duration

interface FileManagementService {
    fun getFileUploadUrl(filenameExtension: String): UploadUrl

    data class UploadUrl(
        val url: String,
        val expiryDuration: Duration,
    )
}
