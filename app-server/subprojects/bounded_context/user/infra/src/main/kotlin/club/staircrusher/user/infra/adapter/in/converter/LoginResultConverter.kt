package club.staircrusher.user.infra.adapter.`in`.converter

import club.staircrusher.api.spec.dto.AuthTokensDto
import club.staircrusher.api.spec.dto.LoginResultDto
import club.staircrusher.user.application.port.`in`.use_case.LoginResult
import club.staircrusher.user.domain.model.AuthTokens

fun AuthTokens.toDTO() = AuthTokensDto(
    accessToken = accessToken,
)

fun LoginResult.toDTO() = LoginResultDto(
    authTokens = authTokens.toDTO(),
    user = user.toDTO(),
)
