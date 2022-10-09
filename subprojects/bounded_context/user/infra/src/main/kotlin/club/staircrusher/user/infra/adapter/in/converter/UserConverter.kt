package club.staircrusher.user.infra.adapter.`in`.converter

import club.staircrusher.user.domain.entity.User

fun User.toDTO() = club.staircrusher.api.spec.dto.User(
    id = id,
    nickname = nickname,
    instagramId = instagramId,
)