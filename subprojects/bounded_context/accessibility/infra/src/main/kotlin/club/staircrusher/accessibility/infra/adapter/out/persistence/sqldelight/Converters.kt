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
import club.staircrusher.infra.persistence.sqldelight.query.accessibility.FindByUserAndBuildingAccessibilityAndNotDeleted
import club.staircrusher.stdlib.time.toOffsetDateTime

fun Building_accessibility.toDomainModel() = BuildingAccessibility(
    id = id,
    buildingId = building_id,
    entranceStairInfo = StairInfo.valueOf(entrance_stair_info),
    hasSlope = has_slope,
    hasElevator = has_elevator,
    elevatorStairInfo = StairInfo.valueOf(elevator_stair_info),
    userId = user_id,
    createdAt = created_at.toInstant(),
)

fun BuildingAccessibility.toPersistenceModel() = Building_accessibility(
    id = id,
    building_id = buildingId,
    entrance_stair_info = entranceStairInfo.name,
    has_slope = hasSlope,
    has_elevator = hasElevator,
    elevator_stair_info = elevatorStairInfo.name,
    user_id = userId,
    created_at = createdAt.toOffsetDateTime(),
    updated_at = createdAt.toOffsetDateTime(),
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
        hasSlope = has_slope,
        hasElevator = has_elevator,
        elevatorStairInfo = StairInfo.valueOf(elevator_stair_info),
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
        hasSlope = has_slope,
        hasElevator = has_elevator,
        elevatorStairInfo = StairInfo.valueOf(elevator_stair_info),
        userId = user_id_,
        createdAt = created_at_.toInstant(),
    )
    return BuildingAccessibilityUpvote(
        id = id,
        userId = user_id,
        buildingAccessibility = buildingAccessibility,
        createdAt = created_at.toInstant(),
        deletedAt = deleted_at.toInstant(),
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

fun Place_accessibility.toDomainModel() = PlaceAccessibility(
    id = id,
    placeId = place_id,
    isFirstFloor = is_first_floor,
    stairInfo = StairInfo.valueOf(stair_info),
    hasSlope = has_slope,
    userId = user_id,
    createdAt = created_at.toInstant(),
)

fun PlaceAccessibility.toPersistenceModel() = Place_accessibility(
    id = id,
    place_id = placeId,
    is_first_floor = isFirstFloor,
    stair_info = stairInfo.name,
    has_slope = hasSlope,
    user_id = userId,
    created_at = createdAt.toOffsetDateTime(),
    updated_at = createdAt.toOffsetDateTime(),
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
