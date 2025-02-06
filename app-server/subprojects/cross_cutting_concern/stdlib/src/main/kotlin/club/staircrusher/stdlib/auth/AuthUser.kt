package club.staircrusher.stdlib.auth

data class AuthUser(
    val id: String,
    val type: String,
    val nickname: String?,
    val instagramId: String?,
)
