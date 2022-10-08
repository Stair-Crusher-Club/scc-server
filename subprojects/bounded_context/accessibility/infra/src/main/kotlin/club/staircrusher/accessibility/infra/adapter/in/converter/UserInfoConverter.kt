package club.staircrusher.accessibility.infra.adapter.`in`.converter

import club.staircrusher.accessibility.application.UserInfo
import club.staircrusher.api.spec.dto.User

fun UserInfo.toDTO() = User(
    id = userId,
    nickname = nickname,
    instagramId = instagramId,
)
