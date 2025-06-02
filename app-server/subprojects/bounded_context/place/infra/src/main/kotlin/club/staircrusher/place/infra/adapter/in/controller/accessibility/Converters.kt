@file:Suppress("TooManyFunctions")

package club.staircrusher.place.infra.adapter.`in`.controller.accessibility

import club.staircrusher.api.converter.toDTO
import club.staircrusher.api.spec.dto.AccessibilityInfoDto
import club.staircrusher.api.spec.dto.AccessibilityRegistererDto
import club.staircrusher.api.spec.dto.EpochMillisTimestamp
import club.staircrusher.api.spec.dto.PlaceAccessibilityDeletionInfo
import club.staircrusher.api.spec.dto.RegisterBuildingAccessibilityRequestDto
import club.staircrusher.api.spec.dto.RegisterPlaceAccessibilityRequestDto
import club.staircrusher.challenge.domain.model.ChallengeCrusherGroup
import club.staircrusher.challenge.infra.adapter.`in`.controller.toDTO
import club.staircrusher.place.application.port.`in`.accessibility.result.GetAccessibilityResult
import club.staircrusher.place.application.port.out.accessibility.persistence.BuildingAccessibilityRepository
import club.staircrusher.place.application.port.out.accessibility.persistence.PlaceAccessibilityRepository
import club.staircrusher.place.application.result.AccessibilityRegisterer
import club.staircrusher.place.domain.model.accessibility.AccessibilityRank
import club.staircrusher.place.domain.model.accessibility.BuildingAccessibility
import club.staircrusher.place.domain.model.accessibility.BuildingAccessibilityComment
import club.staircrusher.place.domain.model.accessibility.EntranceDoorType
import club.staircrusher.place.domain.model.accessibility.AccessibilityImage
import club.staircrusher.place.domain.model.accessibility.PlaceAccessibility
import club.staircrusher.place.domain.model.accessibility.PlaceAccessibilityComment
import club.staircrusher.place.domain.model.accessibility.StairHeightLevel
import club.staircrusher.place.domain.model.accessibility.StairInfo
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
    authUser: AuthUser?,
    challengeCrusherGroup: ChallengeCrusherGroup?
) = club.staircrusher.api.spec.dto.BuildingAccessibility(
    id = id,
    entranceStairInfo = entranceStairInfo.toDTO(),
    entranceStairHeightLevel = entranceStairHeightLevel?.toDTO(),
    entranceImageUrls = newEntranceAccessibilityImages.map { it.blurredImageUrl ?: it.originalImageUrl },
    entranceImages = newEntranceAccessibilityImages.map { it.toDTO() },
    hasSlope = hasSlope,
    hasElevator = hasElevator,
    entranceDoorTypes = entranceDoorTypes?.map { it.toDTO() },
    elevatorStairInfo = elevatorStairInfo.toDTO(),
    elevatorStairHeightLevel = elevatorStairHeightLevel?.toDTO(),
    elevatorImageUrls = newElevatorAccessibilityImages.map { it.blurredImageUrl ?: it.originalImageUrl },
    elevatorImages = newElevatorAccessibilityImages.map { it.toDTO() },
    buildingId = buildingId,
    isUpvoted = isUpvoted,
    totalUpvoteCount = totalUpvoteCount,
    registeredUserName = registeredUserName,
    isDeletable = isDeletable(authUser?.id),
    challengeCrusherGroup = challengeCrusherGroup?.toDTO(),
    createdAt = EpochMillisTimestamp(createdAt.toEpochMilli())
)

fun GetAccessibilityResult.toDTO(authUser: AuthUser?) =
    AccessibilityInfoDto(
        buildingAccessibility = buildingAccessibility?.let {
            it.value.toDTO(
                isUpvoted = buildingAccessibilityUpvoteInfo?.isUpvoted ?: false,
                totalUpvoteCount = buildingAccessibilityUpvoteInfo?.totalUpvoteCount ?: 0,
                registeredUserName = it.accessibilityRegisterer?.nickname,
                authUser = authUser,
                challengeCrusherGroup = buildingAccessibilityChallengeCrusherGroup
            )
        },
        placeAccessibility = placeAccessibility?.let {
            it.value.toDTO(
                registeredAccessibilityRegisterer = it.accessibilityRegisterer,
                authUser = authUser,
                isLastInBuilding = isLastPlaceAccessibilityInBuilding,
                challengeCrusherGroup = placeAccessibilityChallengeCrusherGroup
            )
        },
        buildingAccessibilityComments = buildingAccessibilityComments.map {
            it.value.toDTO(accessibilityRegisterer = it.accessibilityRegisterer)
        },
        placeAccessibilityComments = placeAccessibilityComments.map {
            it.value.toDTO(accessibilityRegisterer = it.accessibilityRegisterer)
        },
        hasOtherPlacesToRegisterInBuilding = hasOtherPlacesToRegisterInSameBuilding,
        isFavoritePlace = isFavoritePlace,
        totalFavoriteCount = totalFavoriteCount,
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
    challengeCrusherGroup: ChallengeCrusherGroup?,
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
        imageUrls = images.map { it.originalImageUrl },
        images = images.map { it.toDTO() },
        stairInfo = stairInfo.toDTO(),
        stairHeightLevel = stairHeightLevel?.toDTO(),
        hasSlope = hasSlope,
        entranceDoorTypes = entranceDoorTypes?.map { it.toDTO() },
        registeredUserName = registeredAccessibilityRegisterer?.nickname,
        isDeletable = isDeletable(authUser?.id),
        challengeCrusherGroup = challengeCrusherGroup?.toDTO(),
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
    club.staircrusher.api.spec.dto.StairInfo.UNDEFINED -> StairInfo.UNDEFINED
    club.staircrusher.api.spec.dto.StairInfo.NONE -> StairInfo.NONE
    club.staircrusher.api.spec.dto.StairInfo.ONE -> StairInfo.ONE
    club.staircrusher.api.spec.dto.StairInfo.TWO_TO_FIVE -> StairInfo.TWO_TO_FIVE
    club.staircrusher.api.spec.dto.StairInfo.OVER_SIX -> StairInfo.OVER_SIX
}

fun StairInfo.toDTO() = when (this) {
    StairInfo.UNDEFINED -> club.staircrusher.api.spec.dto.StairInfo.UNDEFINED
    StairInfo.NONE -> club.staircrusher.api.spec.dto.StairInfo.NONE
    StairInfo.ONE -> club.staircrusher.api.spec.dto.StairInfo.ONE
    StairInfo.TWO_TO_FIVE -> club.staircrusher.api.spec.dto.StairInfo.TWO_TO_FIVE
    StairInfo.OVER_SIX -> club.staircrusher.api.spec.dto.StairInfo.OVER_SIX
}

fun club.staircrusher.api.spec.dto.StairHeightLevel.toModel() = when (this) {
    club.staircrusher.api.spec.dto.StairHeightLevel.HALF_THUMB -> StairHeightLevel.HALF_THUMB
    club.staircrusher.api.spec.dto.StairHeightLevel.THUMB -> StairHeightLevel.THUMB
    club.staircrusher.api.spec.dto.StairHeightLevel.OVER_THUMB -> StairHeightLevel.OVER_THUMB
}

fun StairHeightLevel.toDTO() = when (this) {
    StairHeightLevel.HALF_THUMB -> club.staircrusher.api.spec.dto.StairHeightLevel.HALF_THUMB
    StairHeightLevel.THUMB -> club.staircrusher.api.spec.dto.StairHeightLevel.THUMB
    StairHeightLevel.OVER_THUMB -> club.staircrusher.api.spec.dto.StairHeightLevel.OVER_THUMB
}

fun club.staircrusher.api.spec.dto.EntranceDoorType.toModel() = when (this) {
    club.staircrusher.api.spec.dto.EntranceDoorType.NONE -> EntranceDoorType.None
    club.staircrusher.api.spec.dto.EntranceDoorType.HINGED -> EntranceDoorType.Hinged
    club.staircrusher.api.spec.dto.EntranceDoorType.SLIDING -> EntranceDoorType.Sliding
    club.staircrusher.api.spec.dto.EntranceDoorType.REVOLVING -> EntranceDoorType.Revolving
    club.staircrusher.api.spec.dto.EntranceDoorType.AUTOMATIC -> EntranceDoorType.Automatic
    club.staircrusher.api.spec.dto.EntranceDoorType.ETC -> EntranceDoorType.ETC
}

fun EntranceDoorType.toDTO() = when (this) {
    EntranceDoorType.None -> club.staircrusher.api.spec.dto.EntranceDoorType.NONE
    EntranceDoorType.Hinged -> club.staircrusher.api.spec.dto.EntranceDoorType.HINGED
    EntranceDoorType.Sliding -> club.staircrusher.api.spec.dto.EntranceDoorType.SLIDING
    EntranceDoorType.Revolving -> club.staircrusher.api.spec.dto.EntranceDoorType.REVOLVING
    EntranceDoorType.Automatic -> club.staircrusher.api.spec.dto.EntranceDoorType.AUTOMATIC
    EntranceDoorType.ETC -> club.staircrusher.api.spec.dto.EntranceDoorType.ETC
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

fun AccessibilityImage.toDTO() = club.staircrusher.api.spec.dto.ImageDto(
    imageUrl = SccCdn.forAccessibilityImage(blurredImageUrl ?: originalImageUrl),
    thumbnailUrl = thumbnailUrl?.let { SccCdn.forAccessibilityImage(it) },
)
