package club.staircrusher.accessibility.application

import club.staircrusher.user.domain.model.UserProfile

data class AccessibilityRegisterer(
    val userId: String,
    val nickname: String,
    val instagramId: String?,
)

fun UserProfile.toDomainModel() = AccessibilityRegisterer(
    userId = userId,
    nickname = nickname,
    instagramId = instagramId,
)
