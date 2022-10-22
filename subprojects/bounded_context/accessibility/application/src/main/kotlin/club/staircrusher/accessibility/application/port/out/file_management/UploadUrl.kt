package club.staircrusher.accessibility.application.port.out.file_management

import java.time.Instant

data class UploadUrl(
    val url: String,
    val expireAt: Instant,
)
