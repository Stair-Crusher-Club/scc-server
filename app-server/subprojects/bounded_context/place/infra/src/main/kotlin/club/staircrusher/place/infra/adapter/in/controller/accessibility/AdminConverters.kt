package club.staircrusher.place.infra.adapter.`in`.controller.accessibility

import club.staircrusher.admin_api.converter.toDTO
import club.staircrusher.admin_api.spec.dto.AccessibilityAllowedRegionDTO
import club.staircrusher.admin_api.spec.dto.AdminBuildingAccessibilityDTO
import club.staircrusher.admin_api.spec.dto.AdminEntranceDoorType
import club.staircrusher.admin_api.spec.dto.AdminImageDTO
import club.staircrusher.admin_api.spec.dto.AdminPlaceAccessibilityDTO
import club.staircrusher.admin_api.spec.dto.AdminStairHeightLevel
import club.staircrusher.admin_api.spec.dto.AdminStairInfoDTO
import club.staircrusher.place.domain.model.accessibility.AccessibilityAllowedRegion
import club.staircrusher.place.domain.model.accessibility.AccessibilityImage
import club.staircrusher.place.domain.model.accessibility.BuildingAccessibility
import club.staircrusher.place.domain.model.accessibility.EntranceDoorType
import club.staircrusher.place.domain.model.accessibility.PlaceAccessibility
import club.staircrusher.place.domain.model.accessibility.StairHeightLevel
import club.staircrusher.place.domain.model.accessibility.StairInfo
import club.staircrusher.spring_web.cdn.SccCdn

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
    floors = floors,
    isFirstFloor = isFirstFloor,
    isStairOnlyOption = isStairOnlyOption,
    stairInfo = stairInfo.toAdminDTO(),
    stairHeightLevel = stairHeightLevel?.toAdminDTO(),
    hasSlope = hasSlope,
    entranceDoorTypes = entranceDoorTypes?.map { it.toAdminDTO() },
    imageUrls = imageUrls,
    images = images.map { it.toAdminDTO() },
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
    entranceStairHeightLevel = entranceStairHeightLevel?.toAdminDTO(),
    entranceImageUrls = entranceImageUrls,
    entranceImages = entranceImages.map { it.toAdminDTO() },
    hasSlope = hasSlope,
    hasElevator = hasElevator,
    entranceDoorTypes = entranceDoorTypes?.map { it.toAdminDTO() },
    elevatorStairInfo = elevatorStairInfo.toAdminDTO(),
    elevatorStairHeightLevel = elevatorStairHeightLevel?.toAdminDTO(),
    elevatorImageUrls = elevatorImageUrls,
    elevatorImages = elevatorImages.map { it.toAdminDTO() },
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

fun AdminStairInfoDTO.toModel() = when (this) {
    AdminStairInfoDTO.UNDEFINED -> StairInfo.UNDEFINED
    AdminStairInfoDTO.NONE -> StairInfo.NONE
    AdminStairInfoDTO.ONE -> StairInfo.ONE
    AdminStairInfoDTO.TWO_TO_FIVE -> StairInfo.TWO_TO_FIVE
    AdminStairInfoDTO.OVER_SIX -> StairInfo.OVER_SIX
}

fun StairHeightLevel.toAdminDTO() = when (this) {
    StairHeightLevel.HALF_THUMB -> AdminStairHeightLevel.HALF_THUMB
    StairHeightLevel.THUMB -> AdminStairHeightLevel.THUMB
    StairHeightLevel.OVER_THUMB -> AdminStairHeightLevel.OVER_THUMB
}

fun AdminStairHeightLevel.toModel() = when (this) {
    AdminStairHeightLevel.HALF_THUMB -> StairHeightLevel.HALF_THUMB
    AdminStairHeightLevel.THUMB -> StairHeightLevel.THUMB
    AdminStairHeightLevel.OVER_THUMB -> StairHeightLevel.OVER_THUMB
}

fun EntranceDoorType.toAdminDTO() = when (this) {
    EntranceDoorType.None -> AdminEntranceDoorType.NONE
    EntranceDoorType.Hinged -> AdminEntranceDoorType.HINGED
    EntranceDoorType.Sliding -> AdminEntranceDoorType.SLIDING
    EntranceDoorType.Revolving -> AdminEntranceDoorType.REVOLVING
    EntranceDoorType.Automatic -> AdminEntranceDoorType.AUTOMATIC
    EntranceDoorType.ETC -> AdminEntranceDoorType.ETC
}

fun AdminEntranceDoorType.toModel() = when (this) {
    AdminEntranceDoorType.NONE -> EntranceDoorType.None
    AdminEntranceDoorType.HINGED -> EntranceDoorType.Hinged
    AdminEntranceDoorType.SLIDING -> EntranceDoorType.Sliding
    AdminEntranceDoorType.REVOLVING -> EntranceDoorType.Revolving
    AdminEntranceDoorType.AUTOMATIC -> EntranceDoorType.Automatic
    AdminEntranceDoorType.ETC -> EntranceDoorType.ETC
}

fun AccessibilityImage.toAdminDTO() = AdminImageDTO(
    imageUrl = SccCdn.forAccessibilityImage(imageUrl),
    thumbnailUrl = thumbnailUrl?.let { SccCdn.forAccessibilityImage(it) },
)
