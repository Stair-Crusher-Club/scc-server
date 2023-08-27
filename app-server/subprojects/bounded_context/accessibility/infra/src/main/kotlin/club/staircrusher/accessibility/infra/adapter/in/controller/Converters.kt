@file:Suppress("TooManyFunctions")

package club.staircrusher.accessibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.application.AccessibilityRegisterer
import club.staircrusher.accessibility.application.port.`in`.result.GetAccessibilityResult
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.accessibility.domain.model.AccessibilityRank
import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.BuildingAccessibilityComment
import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.accessibility.domain.model.PlaceAccessibilityComment
import club.staircrusher.accessibility.domain.model.StairInfo
import club.staircrusher.api.converter.toDTO
import club.staircrusher.api.spec.dto.AccessibilityInfoDto
import club.staircrusher.api.spec.dto.AccessibilityRegistererDto
import club.staircrusher.api.spec.dto.PlaceAccessibilityDeletionInfo
import club.staircrusher.api.spec.dto.RegisterBuildingAccessibilityRequestDto
import club.staircrusher.api.spec.dto.RegisterPlaceAccessibilityRequestDto
import club.staircrusher.stdlib.auth.AuthUser

fun BuildingAccessibilityComment.toDTO(accessibilityRegisterer: AccessibilityRegisterer?) = club.staircrusher.api.spec.dto.BuildingAccessibilityComment(
    id = id,
    buildingId = buildingId,
    comment = comment,
    createdAt = createdAt.toDTO(),
    user = accessibilityRegisterer?.toDTO(),
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

fun GetAccessibilityResult.toDTO(authUser: AuthUser?) = AccessibilityInfoDto(
    buildingAccessibility = buildingAccessibility?.let {
        it.value.toDTO(
            isUpvoted = buildingAccessibilityUpvoteInfo?.isUpvoted ?: false,
            totalUpvoteCount = buildingAccessibilityUpvoteInfo?.totalUpvoteCount ?: 0,
            registeredUserName = it.accessibilityRegisterer?.nickname,
        )
    },
    placeAccessibility = placeAccessibility?.let {
        it.value.toDTO(
            registeredAccessibilityRegisterer = it.accessibilityRegisterer,
            authUser = authUser,
            isLastInBuilding = isLastPlaceAccessibilityInBuilding,
        )
    },
    buildingAccessibilityComments = buildingAccessibilityComments.map {
        it.value.toDTO(accessibilityRegisterer = it.accessibilityRegisterer)
    },
    placeAccessibilityComments = placeAccessibilityComments.map {
        it.value.toDTO(accessibilityRegisterer = it.accessibilityRegisterer)
    },
    hasOtherPlacesToRegisterInBuilding = hasOtherPlacesToRegisterInSameBuilding,
)

fun RegisterBuildingAccessibilityRequestDto.toModel(userId: String?) =
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

fun PlaceAccessibilityComment.toDTO(accessibilityRegisterer: AccessibilityRegisterer?) = club.staircrusher.api.spec.dto.PlaceAccessibilityComment(
    id = id,
    placeId = placeId,
    comment = comment,
    createdAt = createdAt.toDTO(),
    user = accessibilityRegisterer?.toDTO(),
)

fun PlaceAccessibility.toDTO(
    registeredAccessibilityRegisterer: AccessibilityRegisterer?,
    authUser: AuthUser?,
    isLastInBuilding: Boolean,
) = club.staircrusher.api.spec.dto.PlaceAccessibility(
    id = id,
    isFirstFloor = isFirstFloor,
    stairInfo = stairInfo.toDTO(),
    hasSlope = hasSlope,
    imageUrls = imageUrls,
    placeId = placeId,
    registeredUserName = registeredAccessibilityRegisterer?.nickname,
    deletionInfo = if (isDeletable(authUser?.id)) {
        PlaceAccessibilityDeletionInfo(
            isLastInBuilding = isLastInBuilding,
        )
    } else {
        null
    }
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

fun RegisterPlaceAccessibilityRequestDto.toModel(userId: String?) =
    PlaceAccessibilityRepository.CreateParams(
        placeId = placeId,
        isFirstFloor = isFirstFloor,
        stairInfo = stairInfo.toModel(),
        hasSlope = hasSlope,
        imageUrls = imageUrls,
        userId = userId,
    )

fun AccessibilityRegisterer.toDTO() = AccessibilityRegistererDto(
    id = userId,
    nickname = nickname,
    instagramId = instagramId,
)

fun AccessibilityRank.toDTO(accessibilityRegisterer: AccessibilityRegisterer) = club.staircrusher.api.spec.dto.AccessibilityRankDto(
    user = accessibilityRegisterer.toDTO(),
    rank = rank,
    conqueredCount = conqueredCount,
)
