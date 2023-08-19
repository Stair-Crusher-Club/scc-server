package club.staircrusher.user.infra.adapter.`in`.converter

import club.staircrusher.api.spec.dto.LoginResultDto
import club.staircrusher.user.application.port.`in`.dto.LoginResult

fun LoginResult.toDTO() = LoginResultDto(
    accessToken = accessToken,
)
