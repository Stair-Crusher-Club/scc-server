package club.staircrusher.accessibility.infra.adapter.`in`.converter

import club.staircrusher.accessibility.domain.model.PlaceAccessibility

fun PlaceAccessibility.toDTO(registeredUserName: String?) = club.staircrusher.api.spec.dto.PlaceAccessibility(
    id = id,
    isFirstFloor = isFirstFloor,
    stairInfo = stairInfo.toDTO(),
    hasSlope = hasSlope,
    placeId = placeId,
    registeredUserName = registeredUserName,
)
