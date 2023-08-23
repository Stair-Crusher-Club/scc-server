package club.staircrusher.user.infra.adapter.`in`.converter

import club.staircrusher.api.spec.dto.LoginResultDto
import club.staircrusher.user.domain.model.LoginResult

fun LoginResult.toDTO() = LoginResultDto(
    accessToken = accessToken,
)
