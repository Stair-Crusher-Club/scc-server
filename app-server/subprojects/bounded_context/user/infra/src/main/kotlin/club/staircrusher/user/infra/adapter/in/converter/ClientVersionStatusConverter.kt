package club.staircrusher.user.infra.adapter.`in`.converter

import club.staircrusher.api.spec.dto.GetClientVersionStatusResponseDto
import club.staircrusher.user.domain.model.ClientVersionStatus

fun ClientVersionStatus.Status.toDTO() =
    when (this) {
        ClientVersionStatus.Status.STABLE -> GetClientVersionStatusResponseDto.Status.sTABLE
        ClientVersionStatus.Status.UPGRADE_RECOMMENDED -> GetClientVersionStatusResponseDto.Status.uPGRADERECOMMENDED
        ClientVersionStatus.Status.UPGRADE_NEEDED -> GetClientVersionStatusResponseDto.Status.uPGRADENEEDED
    }
