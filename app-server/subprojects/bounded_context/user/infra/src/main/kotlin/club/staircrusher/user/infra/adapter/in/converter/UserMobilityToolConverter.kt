package club.staircrusher.user.infra.adapter.`in`.converter

import club.staircrusher.api.spec.dto.UserMobilityToolDto
import club.staircrusher.user.domain.model.UserMobilityTool

fun UserMobilityToolDto.toModel() = when (this) {
    UserMobilityToolDto.mANUALWHEELCHAIR -> UserMobilityTool.MANUAL_WHEELCHAIR
    UserMobilityToolDto.eLECTRICWHEELCHAIR -> UserMobilityTool.ELECTRIC_WHEELCHAIR
    UserMobilityToolDto.mANUALANDELECTRICWHEELCHAIR -> UserMobilityTool.MANUAL_AND_ELECTRIC_WHEELCHAIR
    UserMobilityToolDto.sTROLLER -> UserMobilityTool.STROLLER
    UserMobilityToolDto.wALKINGASSISTANCEDEVICE -> UserMobilityTool.WALKING_ASSISTANCE_DEVICE
}

fun UserMobilityTool.toDTO() = when (this) {
    UserMobilityTool.MANUAL_WHEELCHAIR -> UserMobilityToolDto.mANUALWHEELCHAIR
    UserMobilityTool.ELECTRIC_WHEELCHAIR -> UserMobilityToolDto.eLECTRICWHEELCHAIR
    UserMobilityTool.MANUAL_AND_ELECTRIC_WHEELCHAIR -> UserMobilityToolDto.mANUALANDELECTRICWHEELCHAIR
    UserMobilityTool.STROLLER -> UserMobilityToolDto.sTROLLER
    UserMobilityTool.WALKING_ASSISTANCE_DEVICE -> UserMobilityToolDto.wALKINGASSISTANCEDEVICE
}
