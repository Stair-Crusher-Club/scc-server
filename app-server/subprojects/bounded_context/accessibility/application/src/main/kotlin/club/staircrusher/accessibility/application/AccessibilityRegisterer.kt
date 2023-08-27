package club.staircrusher.accessibility.application

import club.staircrusher.user.domain.model.User

data class AccessibilityRegisterer(
    val userId: String,
    val nickname: String,
    val instagramId: String?,
)

fun User.toDomainModel() = AccessibilityRegisterer(
    userId = id,
    nickname = nickname,
    instagramId = instagramId,
)
