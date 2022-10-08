package club.staircrusher.accessibility.infra.adapter.`in`.converter

import club.staircrusher.accessibility.domain.service.BuildingAccessibilityService
import club.staircrusher.api.spec.dto.RegisterAccessibilityPostRequestBuildingAccessibilityParams


fun RegisterAccessibilityPostRequestBuildingAccessibilityParams.toModel(userId: String?) = BuildingAccessibilityService.CreateParams(
    buildingId = buildingId,
    entranceStairInfo = entranceStairInfo.toModel(),
    hasSlope = hasSlope,
    hasElevator = hasElevator,
    elevatorStairInfo = elevatorStairInfo.toModel(),
    userId = userId,
)
