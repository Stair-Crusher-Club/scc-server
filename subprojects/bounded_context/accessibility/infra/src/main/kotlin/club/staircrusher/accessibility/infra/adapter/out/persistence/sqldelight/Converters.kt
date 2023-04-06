@file:Suppress("TooManyFunctions")

package club.staircrusher.accessibility.infra.adapter.out.persistence.sqldelight

import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.BuildingAccessibilityComment
import club.staircrusher.accessibility.domain.model.BuildingAccessibilityUpvote
import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.accessibility.domain.model.PlaceAccessibilityComment
import club.staircrusher.accessibility.domain.model.StairInfo
import club.staircrusher.infra.persistence.sqldelight.migration.Building_accessibility
import club.staircrusher.infra.persistence.sqldelight.migration.Building_accessibility_comment
import club.staircrusher.infra.persistence.sqldelight.migration.Building_accessibility_upvote
import club.staircrusher.infra.persistence.sqldelight.migration.Place_accessibility
import club.staircrusher.infra.persistence.sqldelight.migration.Place_accessibility_comment
import club.staircrusher.infra.persistence.sqldelight.query.accessibility.BuildingAccessibilityUpvoteFindById
import club.staircrusher.infra.persistence.sqldelight.query.accessibility.FindByBuildingIds
import club.staircrusher.infra.persistence.sqldelight.query.accessibility.FindByEupMyeonDong
import club.staircrusher.infra.persistence.sqldelight.query.accessibility.FindByPlaceId
import club.staircrusher.infra.persistence.sqldelight.query.accessibility.FindByUserAndBuildingAccessibilityAndNotDeleted
import club.staircrusher.infra.persistence.sqldelight.query.accessibility.FindByUserId
import club.staircrusher.infra.persistence.sqldelight.query.accessibility.placeAccessibility.FindByBuildingId
import club.staircrusher.stdlib.time.toOffsetDateTime

fun club.staircrusher.infra.persistence.sqldelight.query.accessibility.buildingAccessibility.FindById.toDomainModel() = BuildingAccessibility(
    id = id,
    buildingId = building_id,
    entranceStairInfo = StairInfo.valueOf(entrance_stair_info),
    entranceImageUrls = entrance_image_urls,
    hasSlope = has_slope,
    hasElevator = has_elevator,
    elevatorStairInfo = StairInfo.valueOf(elevator_stair_info),
    elevatorImageUrls = elevator_image_urls,
    userId = user_id,
    createdAt = created_at.toInstant(),
)

fun club.staircrusher.infra.persistence.sqldelight.query.accessibility.buildingAccessibility.FindByBuildingId.toDomainModel() = BuildingAccessibility(
    id = id,
    buildingId = building_id,
    entranceStairInfo = StairInfo.valueOf(entrance_stair_info),
    entranceImageUrls = entrance_image_urls,
    hasSlope = has_slope,
    hasElevator = has_elevator,
    elevatorStairInfo = StairInfo.valueOf(elevator_stair_info),
    elevatorImageUrls = elevator_image_urls,
    userId = user_id,
    createdAt = created_at.toInstant(),
)

fun FindByBuildingIds.toDomainModel() = BuildingAccessibility(
    id = id,
    buildingId = building_id,
    entranceStairInfo = StairInfo.valueOf(entrance_stair_info),
    entranceImageUrls = entrance_image_urls,
    hasSlope = has_slope,
    hasElevator = has_elevator,
    elevatorStairInfo = StairInfo.valueOf(elevator_stair_info),
    elevatorImageUrls = elevator_image_urls,
    userId = user_id,
    createdAt = created_at.toInstant(),
)

fun club.staircrusher.infra.persistence.sqldelight.query.accessibility.buildingAccessibility.FindByPlaceIds.toDomainModel() = BuildingAccessibility(
    id = id,
    buildingId = building_id,
    entranceStairInfo = StairInfo.valueOf(entrance_stair_info),
    entranceImageUrls = entrance_image_urls,
    hasSlope = has_slope,
    hasElevator = has_elevator,
    elevatorStairInfo = StairInfo.valueOf(elevator_stair_info),
    elevatorImageUrls = elevator_image_urls,
    userId = user_id,
    createdAt = created_at.toInstant(),
)

fun FindByEupMyeonDong.toDomainModel() = BuildingAccessibility(
    id = id,
    buildingId = building_id,
    entranceStairInfo = StairInfo.valueOf(entrance_stair_info),
    entranceImageUrls = entrance_image_urls,
    hasSlope = has_slope,
    hasElevator = has_elevator,
    elevatorStairInfo = StairInfo.valueOf(elevator_stair_info),
    elevatorImageUrls = elevator_image_urls,
    userId = user_id,
    createdAt = created_at.toInstant(),
)

fun BuildingAccessibility.toPersistenceModel() = Building_accessibility(
    id = id,
    building_id = buildingId,
    entrance_stair_info = entranceStairInfo.name,
    entrance_image_urls = entranceImageUrls,
    has_slope = hasSlope,
    has_elevator = hasElevator,
    elevator_stair_info = elevatorStairInfo.name,
    elevator_image_urls = elevatorImageUrls,
    user_id = userId,
    created_at = createdAt.toOffsetDateTime(),
    updated_at = createdAt.toOffsetDateTime(),
    deleted_at = deletedAt?.toOffsetDateTime(),
)

fun Building_accessibility_comment.toDomainModel() = BuildingAccessibilityComment(
    id = id,
    buildingId = building_id,
    userId = user_id,
    comment = comment,
    createdAt = created_at.toInstant(),
)

fun BuildingAccessibilityComment.toPersistenceModel() = Building_accessibility_comment(
    id = id,
    building_id = buildingId,
    user_id = userId,
    comment = comment,
    created_at = createdAt.toOffsetDateTime(),
    updated_at = createdAt.toOffsetDateTime(),
)

fun BuildingAccessibilityUpvoteFindById.toDomainModel(): BuildingAccessibilityUpvote {
    val buildingAccessibility = BuildingAccessibility(
        id = id_,
        buildingId = building_id,
        entranceStairInfo = StairInfo.valueOf(entrance_stair_info),
        entranceImageUrls = entrance_image_urls,
        hasSlope = has_slope,
        hasElevator = has_elevator,
        elevatorStairInfo = StairInfo.valueOf(elevator_stair_info),
        elevatorImageUrls = elevator_image_urls,
        userId = user_id_,
        createdAt = created_at_.toInstant(),
    )
    return BuildingAccessibilityUpvote(
        id = id,
        userId = user_id,
        buildingAccessibility = buildingAccessibility,
        createdAt = created_at.toInstant(),
        deletedAt = deleted_at?.toInstant(),
    )
}

fun FindByUserAndBuildingAccessibilityAndNotDeleted.toDomainModel(): BuildingAccessibilityUpvote {
    val buildingAccessibility = BuildingAccessibility(
        id = id_,
        buildingId = building_id,
        entranceStairInfo = StairInfo.valueOf(entrance_stair_info),
        entranceImageUrls = entrance_image_urls,
        hasSlope = has_slope,
        hasElevator = has_elevator,
        elevatorStairInfo = StairInfo.valueOf(elevator_stair_info),
        elevatorImageUrls = elevator_image_urls,
        userId = user_id_,
        createdAt = created_at_.toInstant(),
    )
    return BuildingAccessibilityUpvote(
        id = id,
        userId = user_id,
        buildingAccessibility = buildingAccessibility,
        createdAt = created_at.toInstant(),
        deletedAt = deleted_at?.toInstant(),
    )
}

fun BuildingAccessibilityUpvote.toPersistenceModel() = Building_accessibility_upvote(
    id = id,
    user_id = userId,
    building_accessibility_id = buildingAccessibility.id,
    created_at = createdAt.toOffsetDateTime(),
    updated_at = createdAt.toOffsetDateTime(),
    deleted_at = deletedAt?.toOffsetDateTime(),
)

fun club.staircrusher.infra.persistence.sqldelight.query.accessibility.placeAccessibility.FindById.toDomainModel() = PlaceAccessibility(
    id = id,
    placeId = place_id,
    isFirstFloor = is_first_floor,
    stairInfo = StairInfo.valueOf(stair_info),
    hasSlope = has_slope,
    imageUrls = image_urls,
    userId = user_id,
    createdAt = created_at.toInstant(),
)

fun club.staircrusher.infra.persistence.sqldelight.query.accessibility.placeAccessibility.FindByPlaceIds.toDomainModel() = PlaceAccessibility(
    id = id,
    placeId = place_id,
    isFirstFloor = is_first_floor,
    stairInfo = StairInfo.valueOf(stair_info),
    hasSlope = has_slope,
    imageUrls = image_urls,
    userId = user_id,
    createdAt = created_at.toInstant(),
)

fun FindByPlaceId.toDomainModel() = PlaceAccessibility(
    id = id,
    placeId = place_id,
    isFirstFloor = is_first_floor,
    stairInfo = StairInfo.valueOf(stair_info),
    hasSlope = has_slope,
    imageUrls = image_urls,
    userId = user_id,
    createdAt = created_at.toInstant(),
)

fun FindByUserId.toDomainModel() = PlaceAccessibility(
    id = id,
    placeId = place_id,
    isFirstFloor = is_first_floor,
    stairInfo = StairInfo.valueOf(stair_info),
    hasSlope = has_slope,
    imageUrls = image_urls,
    userId = user_id,
    createdAt = created_at.toInstant(),
)

fun FindByBuildingId.toDomainModel() = PlaceAccessibility(
    id = id,
    placeId = place_id,
    isFirstFloor = is_first_floor,
    stairInfo = StairInfo.valueOf(stair_info),
    hasSlope = has_slope,
    imageUrls = image_urls,
    userId = user_id,
    createdAt = created_at.toInstant(),
)

fun PlaceAccessibility.toPersistenceModel() = Place_accessibility(
    id = id,
    place_id = placeId,
    is_first_floor = isFirstFloor,
    stair_info = stairInfo.name,
    has_slope = hasSlope,
    image_urls = imageUrls,
    user_id = userId,
    created_at = createdAt.toOffsetDateTime(),
    updated_at = createdAt.toOffsetDateTime(),
    deleted_at = deletedAt?.toOffsetDateTime(),
)

fun Place_accessibility_comment.toDomainModel() = PlaceAccessibilityComment(
    id = id,
    placeId = place_id,
    userId = user_id,
    comment = comment,
    createdAt = created_at.toInstant(),
)

fun PlaceAccessibilityComment.toPersistenceModel() = Place_accessibility_comment(
    id = id,
    place_id = placeId,
    user_id = userId,
    comment = comment,
    created_at = createdAt.toOffsetDateTime(),
    updated_at = createdAt.toOffsetDateTime(),
)
