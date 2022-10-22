package club.staircrusher.accessibility.application.port.out

import java.time.Instant

interface FileManagementService {
    fun getFileUploadUrl(filenameExtension: String): UploadUrl

    data class UploadUrl(
        val url: String,
        val expireAt: Instant,
    )
}
