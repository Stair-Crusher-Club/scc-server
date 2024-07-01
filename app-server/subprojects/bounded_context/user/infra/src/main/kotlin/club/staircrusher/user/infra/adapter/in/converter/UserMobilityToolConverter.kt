package club.staircrusher.user.infra.adapter.`in`.converter

import club.staircrusher.api.spec.dto.UserMobilityToolDto
import club.staircrusher.user.domain.model.UserMobilityTool

fun UserMobilityToolDto.toModel() = when (this) {
    UserMobilityToolDto.MANUAL_WHEELCHAIR -> UserMobilityTool.MANUAL_WHEELCHAIR
    UserMobilityToolDto.ELECTRIC_WHEELCHAIR -> UserMobilityTool.ELECTRIC_WHEELCHAIR
    UserMobilityToolDto.MANUAL_AND_ELECTRIC_WHEELCHAIR -> UserMobilityTool.MANUAL_AND_ELECTRIC_WHEELCHAIR
    UserMobilityToolDto.STROLLER -> UserMobilityTool.STROLLER
    UserMobilityToolDto.PROSTHETIC_FOOT -> UserMobilityTool.PROSTHETIC_FOOT
    UserMobilityToolDto.WALKING_ASSISTANCE_DEVICE -> UserMobilityTool.WALKING_ASSISTANCE_DEVICE
    UserMobilityToolDto.CLUCH -> UserMobilityTool.CLUCH
    UserMobilityToolDto.NONE -> UserMobilityTool.NONE
}

fun UserMobilityTool.toDTO() = when (this) {
    UserMobilityTool.MANUAL_WHEELCHAIR -> UserMobilityToolDto.MANUAL_WHEELCHAIR
    UserMobilityTool.ELECTRIC_WHEELCHAIR -> UserMobilityToolDto.ELECTRIC_WHEELCHAIR
    UserMobilityTool.MANUAL_AND_ELECTRIC_WHEELCHAIR -> UserMobilityToolDto.MANUAL_AND_ELECTRIC_WHEELCHAIR
    UserMobilityTool.STROLLER -> UserMobilityToolDto.STROLLER
    UserMobilityTool.PROSTHETIC_FOOT -> UserMobilityToolDto.PROSTHETIC_FOOT
    UserMobilityTool.WALKING_ASSISTANCE_DEVICE -> UserMobilityToolDto.WALKING_ASSISTANCE_DEVICE
    UserMobilityTool.CLUCH -> UserMobilityToolDto.CLUCH
    UserMobilityTool.NONE -> UserMobilityToolDto.NONE
}
