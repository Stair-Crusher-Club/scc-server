package club.staircrusher.user.domain.entity

import java.time.Instant

data class User(
    val id: String,
    var nickname: String,
    var encryptedPassword: String,
    var instagramId: String?,
    val createdAt: Instant,
)
