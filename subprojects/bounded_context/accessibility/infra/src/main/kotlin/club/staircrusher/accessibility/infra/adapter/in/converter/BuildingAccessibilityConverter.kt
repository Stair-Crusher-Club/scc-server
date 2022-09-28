package club.staircrusher.accessibility.infra.adapter.`in`.converter

import club.staircrusher.accessibility.domain.model.BuildingAccessibility

fun BuildingAccessibility.toDTO(
    isUpvoted: Boolean,
    totalUpvoteCount: Int,
    registeredUserName: String?,
) = club.staircrusher.api.spec.dto.BuildingAccessibility(
    id = id,
    entranceStairInfo = entranceStairInfo.toDTO(),
    hasSlope = hasSlope,
    hasElevator = hasElevator,
    elevatorStairInfo = elevatorStairInfo.toDTO(),
    buildingId = buildingId,
    isUpvoted = isUpvoted,
    totalUpvoteCount = totalUpvoteCount,
    registeredUserName = registeredUserName,
)
