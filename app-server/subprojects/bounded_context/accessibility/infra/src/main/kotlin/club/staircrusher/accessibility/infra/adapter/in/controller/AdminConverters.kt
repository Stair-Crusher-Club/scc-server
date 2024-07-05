package club.staircrusher.accessibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.domain.model.AccessibilityAllowedRegion
import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.accessibility.domain.model.StairInfo
import club.staircrusher.admin_api.converter.toDTO
import club.staircrusher.admin_api.spec.dto.AccessibilityAllowedRegionDTO
import club.staircrusher.admin_api.spec.dto.AdminBuildingAccessibilityDTO
import club.staircrusher.admin_api.spec.dto.AdminPlaceAccessibilityDTO
import club.staircrusher.admin_api.spec.dto.AdminStairInfoDTO

fun AccessibilityAllowedRegion.toDTO() = AccessibilityAllowedRegionDTO(
    id = id,
    boundaryVertices = boundaryVertices.map { it.toDTO() },
    name = name,
)

fun PlaceAccessibility.toAdminDTO(
    placeName: String,
    registeredUserName: String?,
) = AdminPlaceAccessibilityDTO(
    id = id,
    isFirstFloor = isFirstFloor,
    stairInfo = stairInfo.toAdminDTO(),
    hasSlope = hasSlope,
    imageUrls = imageUrls,
    placeName = placeName,
    registeredUserName = registeredUserName,
    createdAtMillis = createdAt.toEpochMilli(),
)

fun BuildingAccessibility.toAdminDTO(
    buildingName: String?,
    registeredUserName: String?,
) = AdminBuildingAccessibilityDTO(
    id = id,
    entranceStairInfo = entranceStairInfo.toAdminDTO(),
    entranceImageUrls = entranceImageUrls,
    hasSlope = hasSlope,
    hasElevator = hasElevator,
    elevatorStairInfo = elevatorStairInfo.toAdminDTO(),
    elevatorImageUrls = elevatorImageUrls,
    buildingName = buildingName,
    registeredUserName = registeredUserName,
    createdAtMillis = createdAt.toEpochMilli(),
)

fun StairInfo.toAdminDTO() = when (this) {
    StairInfo.UNDEFINED -> AdminStairInfoDTO.UNDEFINED
    StairInfo.NONE -> AdminStairInfoDTO.NONE
    StairInfo.ONE -> AdminStairInfoDTO.ONE
    StairInfo.TWO_TO_FIVE -> AdminStairInfoDTO.TWO_TO_FIVE
    StairInfo.OVER_SIX -> AdminStairInfoDTO.OVER_SIX
}
