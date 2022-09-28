package club.staircrusher.accessibility.infra.adapter.`in`.converter

import club.staircrusher.accessibility.domain.model.StairInfo

fun club.staircrusher.api.spec.dto.StairInfo.toModel() = when (this) {
    club.staircrusher.api.spec.dto.StairInfo.uNDEFINED -> StairInfo.UNDEFINED
    club.staircrusher.api.spec.dto.StairInfo.nONE -> StairInfo.NONE
    club.staircrusher.api.spec.dto.StairInfo.oNE -> StairInfo.ONE
    club.staircrusher.api.spec.dto.StairInfo.tWOTOFIVE -> StairInfo.TWO_TO_FIVE
    club.staircrusher.api.spec.dto.StairInfo.oVERSIX -> StairInfo.OVER_SIX
}

fun StairInfo.toDTO() = when (this) {
    StairInfo.UNDEFINED -> club.staircrusher.api.spec.dto.StairInfo.uNDEFINED
    StairInfo.NONE -> club.staircrusher.api.spec.dto.StairInfo.nONE
    StairInfo.ONE -> club.staircrusher.api.spec.dto.StairInfo.oNE
    StairInfo.TWO_TO_FIVE -> club.staircrusher.api.spec.dto.StairInfo.tWOTOFIVE
    StairInfo.OVER_SIX -> club.staircrusher.api.spec.dto.StairInfo.oVERSIX
}
