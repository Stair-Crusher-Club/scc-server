package club.staircrusher.user.domain.model

data class IdentifiedUserVO(
    val account: UserAccount,
    val profile: UserProfile,
)
