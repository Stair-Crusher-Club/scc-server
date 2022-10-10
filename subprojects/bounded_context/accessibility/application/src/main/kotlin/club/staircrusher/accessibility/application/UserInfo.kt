package club.staircrusher.accessibility.application

import club.staircrusher.user.domain.entity.User

data class UserInfo(
    val userId: String,
    val nickname: String,
    val instagramId: String?,
)

fun User.toDomainModel() = UserInfo(
    userId = id,
    nickname = nickname,
    instagramId = instagramId,
)
