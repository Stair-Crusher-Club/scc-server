package club.staircrusher.accessibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.application.UserInfo
import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.BuildingAccessibilityComment
import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.accessibility.domain.model.PlaceAccessibilityComment
import club.staircrusher.accessibility.domain.model.StairInfo
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.api.converter.toDTO
import club.staircrusher.api.spec.dto.RegisterAccessibilityPostRequestBuildingAccessibilityParams
import club.staircrusher.api.spec.dto.RegisterAccessibilityPostRequestPlaceAccessibilityParams
import club.staircrusher.api.spec.dto.User

fun BuildingAccessibilityComment.toDTO(userInfo: UserInfo?) = club.staircrusher.api.spec.dto.BuildingAccessibilityComment(
    id = id,
    buildingId = buildingId,
    comment = comment,
    createdAt = createdAt.toDTO(),
    user = userInfo?.toDTO(),
)

fun BuildingAccessibility.toDTO(
    isUpvoted: Boolean,
    totalUpvoteCount: Int,
    registeredUserName: String?,
) = club.staircrusher.api.spec.dto.BuildingAccessibility(
    id = id,
    entranceStairInfo = entranceStairInfo.toDTO(),
    entranceImageUrls = entranceImageUrls,
    hasSlope = hasSlope,
    hasElevator = hasElevator,
    elevatorStairInfo = elevatorStairInfo.toDTO(),
    elevatorImageUrls = elevatorImageUrls,
    buildingId = buildingId,
    isUpvoted = isUpvoted,
    totalUpvoteCount = totalUpvoteCount,
    registeredUserName = registeredUserName,
)

fun RegisterAccessibilityPostRequestBuildingAccessibilityParams.toModel(userId: String?) =
    BuildingAccessibilityRepository.CreateParams(
        buildingId = buildingId,
        entranceStairInfo = entranceStairInfo.toModel(),
        entranceImageUrls = entranceImageUrls,
        hasSlope = hasSlope,
        hasElevator = hasElevator,
        elevatorStairInfo = elevatorStairInfo.toModel(),
        elevatorImageUrls = elevatorImageUrls,
        userId = userId,
    )

fun PlaceAccessibilityComment.toDTO(userInfo: UserInfo?) = club.staircrusher.api.spec.dto.PlaceAccessibilityComment(
    id = id,
    placeId = placeId,
    comment = comment,
    createdAt = createdAt.toDTO(),
    user = userInfo?.toDTO(),
)

fun PlaceAccessibility.toDTO(registeredUserName: String?) = club.staircrusher.api.spec.dto.PlaceAccessibility(
    id = id,
    isFirstFloor = isFirstFloor,
    stairInfo = stairInfo.toDTO(),
    hasSlope = hasSlope,
    imageUrls = imageUrls,
    placeId = placeId,
    registeredUserName = registeredUserName,
)

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

fun RegisterAccessibilityPostRequestPlaceAccessibilityParams.toModel(userId: String?) =
    PlaceAccessibilityRepository.CreateParams(
        placeId = placeId,
        isFirstFloor = isFirstFloor,
        stairInfo = stairInfo.toModel(),
        hasSlope = hasSlope,
        imageUrls = imageUrls,
        userId = userId,
    )

fun UserInfo.toDTO() = User(
    id = userId,
    nickname = nickname,
    instagramId = instagramId,
)
