package club.staircrusher.user.domain.model

data class AuthTokens(
    val accessToken: String,
    val userId: String,
)
