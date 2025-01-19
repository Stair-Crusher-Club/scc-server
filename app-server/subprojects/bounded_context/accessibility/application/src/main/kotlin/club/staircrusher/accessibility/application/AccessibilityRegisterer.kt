package club.staircrusher.accessibility.application

import club.staircrusher.user.domain.model.IdentifiedUser

data class AccessibilityRegisterer(
    val userId: String,
    val nickname: String,
    val instagramId: String?,
)

fun IdentifiedUser.toDomainModel() = AccessibilityRegisterer(
    userId = id,
    nickname = nickname,
    instagramId = instagramId,
)
