@file:Suppress("TooManyFunctions")

package club.staircrusher.accessibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.application.AccessibilityRegisterer
import club.staircrusher.accessibility.application.port.`in`.result.GetAccessibilityResult
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.accessibility.domain.model.AccessibilityRank
import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.BuildingAccessibilityComment
import club.staircrusher.accessibility.domain.model.EntranceDoorType
import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.accessibility.domain.model.PlaceAccessibilityComment
import club.staircrusher.accessibility.domain.model.StairHeightLevel
import club.staircrusher.accessibility.domain.model.StairInfo
import club.staircrusher.api.converter.toDTO
import club.staircrusher.api.spec.dto.AccessibilityInfoDto
import club.staircrusher.api.spec.dto.AccessibilityRegistererDto
import club.staircrusher.api.spec.dto.EpochMillisTimestamp
import club.staircrusher.api.spec.dto.PlaceAccessibilityDeletionInfo
import club.staircrusher.api.spec.dto.RegisterBuildingAccessibilityRequestDto
import club.staircrusher.api.spec.dto.RegisterPlaceAccessibilityRequestDto
import club.staircrusher.spring_web.cdn.SccCdn
import club.staircrusher.stdlib.auth.AuthUser

fun BuildingAccessibilityComment.toDTO(accessibilityRegisterer: AccessibilityRegisterer?) =
    club.staircrusher.api.spec.dto.BuildingAccessibilityComment(
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
    entranceStairHeightLevel = entranceStairHeightLevel?.toDTO(),
    entranceImageUrls = entranceImageUrls.map { SccCdn.forAccessibilityImage(it) },
    hasSlope = hasSlope,
    hasElevator = hasElevator,
    entranceDoorTypes = entranceDoorTypes?.map { it.toDTO() },
    elevatorStairInfo = elevatorStairInfo.toDTO(),
    elevatorStairHeightLevel = elevatorStairHeightLevel?.toDTO(),
    elevatorImageUrls = elevatorImageUrls.map { SccCdn.forAccessibilityImage(it) },
    buildingId = buildingId,
    isUpvoted = isUpvoted,
    totalUpvoteCount = totalUpvoteCount,
    registeredUserName = registeredUserName,
    createdAt = EpochMillisTimestamp(createdAt.toEpochMilli())
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
        entranceStairHeightLevel = entranceStairHeightLevel?.toModel(),
        entranceImageUrls = entranceImageUrls,
        hasSlope = hasSlope,
        hasElevator = hasElevator,
        entranceDoorTypes = entranceDoorTypes?.map { it.toModel() },
        elevatorStairInfo = elevatorStairInfo.toModel(),
        elevatorStairHeightLevel = elevatorStairHeightLevel?.toModel(),
        elevatorImageUrls = elevatorImageUrls,
        userId = userId,
    )

fun PlaceAccessibilityComment.toDTO(accessibilityRegisterer: AccessibilityRegisterer?) =
    club.staircrusher.api.spec.dto.PlaceAccessibilityComment(
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
): club.staircrusher.api.spec.dto.PlaceAccessibility {
    val floors = if (this.floors.isNullOrEmpty()) {
        if (this.isFirstFloor) listOf(1)
        else null
    } else {
        this.floors
    }
    return club.staircrusher.api.spec.dto.PlaceAccessibility(
        id = id,
        placeId = placeId,
        floors = floors,
        isFirstFloor = isFirstFloor,
        isStairOnlyOption = isStairOnlyOption,
        imageUrls = imageUrls.map { SccCdn.forAccessibilityImage(it) },
        stairInfo = stairInfo.toDTO(),
        stairHeightLevel = stairHeightLevel?.toDTO(),
        hasSlope = hasSlope,
        entranceDoorTypes = entranceDoorTypes?.map { it.toDTO() },
        registeredUserName = registeredAccessibilityRegisterer?.nickname,
        deletionInfo = if (isDeletable(authUser?.id)) {
            PlaceAccessibilityDeletionInfo(
                isLastInBuilding = isLastInBuilding,
            )
        } else {
            null
        },
        createdAt = EpochMillisTimestamp(createdAt.toEpochMilli())
    )
}

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

fun club.staircrusher.api.spec.dto.StairHeightLevel.toModel() = when (this) {
    club.staircrusher.api.spec.dto.StairHeightLevel.hALFTHUMB -> StairHeightLevel.HALF_THUMB
    club.staircrusher.api.spec.dto.StairHeightLevel.tHUMB -> StairHeightLevel.THUMB
    club.staircrusher.api.spec.dto.StairHeightLevel.oVERTHUMB -> StairHeightLevel.OVER_THUMB
}

fun StairHeightLevel.toDTO() = when (this) {
    StairHeightLevel.HALF_THUMB -> club.staircrusher.api.spec.dto.StairHeightLevel.hALFTHUMB
    StairHeightLevel.THUMB -> club.staircrusher.api.spec.dto.StairHeightLevel.tHUMB
    StairHeightLevel.OVER_THUMB -> club.staircrusher.api.spec.dto.StairHeightLevel.oVERTHUMB
}

fun club.staircrusher.api.spec.dto.EntranceDoorType.toModel() = when (this) {
    club.staircrusher.api.spec.dto.EntranceDoorType.none -> EntranceDoorType.None
    club.staircrusher.api.spec.dto.EntranceDoorType.hinged -> EntranceDoorType.Hinged
    club.staircrusher.api.spec.dto.EntranceDoorType.sliding -> EntranceDoorType.Sliding
    club.staircrusher.api.spec.dto.EntranceDoorType.revolving -> EntranceDoorType.Revolving
    club.staircrusher.api.spec.dto.EntranceDoorType.automatic -> EntranceDoorType.Automatic
    club.staircrusher.api.spec.dto.EntranceDoorType.eTC -> EntranceDoorType.ETC
}

fun EntranceDoorType.toDTO() = when (this) {
    EntranceDoorType.None -> club.staircrusher.api.spec.dto.EntranceDoorType.none
    EntranceDoorType.Hinged -> club.staircrusher.api.spec.dto.EntranceDoorType.hinged
    EntranceDoorType.Sliding -> club.staircrusher.api.spec.dto.EntranceDoorType.sliding
    EntranceDoorType.Revolving -> club.staircrusher.api.spec.dto.EntranceDoorType.revolving
    EntranceDoorType.Automatic -> club.staircrusher.api.spec.dto.EntranceDoorType.automatic
    EntranceDoorType.ETC -> club.staircrusher.api.spec.dto.EntranceDoorType.eTC
}

fun RegisterPlaceAccessibilityRequestDto.toModel(userId: String?) =
    PlaceAccessibilityRepository.CreateParams(
        placeId = placeId,
        floors = floors,
        isFirstFloor = isFirstFloor,
        isStairOnlyOption = isStairOnlyOption,
        stairInfo = stairInfo.toModel(),
        stairHeightLevel = stairHeightLevel?.toModel(),
        hasSlope = hasSlope,
        imageUrls = imageUrls,
        entranceDoorTypes = entranceDoorTypes?.map { it.toModel() },
        userId = userId,
    )

fun AccessibilityRegisterer.toDTO() = AccessibilityRegistererDto(
    id = userId,
    nickname = nickname,
    instagramId = instagramId,
)

fun AccessibilityRank.toDTO(accessibilityRegisterer: AccessibilityRegisterer) =
    club.staircrusher.api.spec.dto.AccessibilityRankDto(
        user = accessibilityRegisterer.toDTO(),
        rank = rank,
        conqueredCount = conqueredCount,
    )
