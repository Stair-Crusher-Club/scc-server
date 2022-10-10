package club.staircrusher.accessibility.infra.adapter.`in`.converter

import club.staircrusher.accessibility.domain.service.PlaceAccessibilityService
import club.staircrusher.api.spec.dto.RegisterAccessibilityPostRequestPlaceAccessibilityParams


fun RegisterAccessibilityPostRequestPlaceAccessibilityParams.toModel(userId: String?) = PlaceAccessibilityService.CreateParams(
    placeId = placeId,
    isFirstFloor = isFirstFloor,
    stairInfo = stairInfo.toModel(),
    hasSlope = hasSlope,
    userId = userId,
)
