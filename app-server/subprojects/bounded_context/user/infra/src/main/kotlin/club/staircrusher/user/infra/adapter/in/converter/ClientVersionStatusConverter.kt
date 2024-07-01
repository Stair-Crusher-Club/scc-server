package club.staircrusher.user.infra.adapter.`in`.converter

import club.staircrusher.api.spec.dto.GetClientVersionStatusResponseDto
import club.staircrusher.user.domain.model.ClientVersionStatus

fun ClientVersionStatus.Status.toDTO() =
    when (this) {
        ClientVersionStatus.Status.STABLE -> GetClientVersionStatusResponseDto.Status.STABLE
        ClientVersionStatus.Status.UPGRADE_RECOMMENDED -> GetClientVersionStatusResponseDto.Status.UPGRADE_RECOMMENDED
        ClientVersionStatus.Status.UPGRADE_NEEDED -> GetClientVersionStatusResponseDto.Status.UPGRADE_NEEDED
    }
