@file:Suppress("TooManyFunctions")

package club.staircrusher.accessibility.infra.adapter.out.persistence.sqldelight

import club.staircrusher.accessibility.domain.model.AccessibilityAllowedRegion
import club.staircrusher.accessibility.domain.model.AccessibilityRank
import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.BuildingAccessibilityComment
import club.staircrusher.accessibility.domain.model.BuildingAccessibilityUpvote
import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.accessibility.domain.model.PlaceAccessibilityComment
import club.staircrusher.accessibility.domain.model.PlaceAccessibilityUpvote
import club.staircrusher.accessibility.domain.model.StairInfo
import club.staircrusher.infra.persistence.sqldelight.migration.Accessibility_allowed_region
import club.staircrusher.infra.persistence.sqldelight.migration.Accessibility_rank
import club.staircrusher.infra.persistence.sqldelight.migration.Building_accessibility
import club.staircrusher.infra.persistence.sqldelight.migration.Building_accessibility_comment
import club.staircrusher.infra.persistence.sqldelight.migration.Building_accessibility_upvote
import club.staircrusher.infra.persistence.sqldelight.migration.Place_accessibility
import club.staircrusher.infra.persistence.sqldelight.migration.Place_accessibility_comment
import club.staircrusher.infra.persistence.sqldelight.migration.Place_accessibility_upvote
import club.staircrusher.infra.persistence.sqldelight.query.accessibility.BuildingAccessibilityUpvoteFindById
import club.staircrusher.infra.persistence.sqldelight.query.accessibility.FindByUserAndBuildingAccessibilityAndNotDeleted
import club.staircrusher.infra.persistence.sqldelight.query.accessibility.FindUpvoteById
import club.staircrusher.infra.persistence.sqldelight.query.accessibility.FindUpvoteByUserIdAndPlaceAccessilbityIdAndNotDeleted
import club.staircrusher.stdlib.time.toOffsetDateTime

fun Building_accessibility.toDomainModel() = BuildingAccessibility(
    id = id,
    buildingId = building_id,
    entranceStairInfo = StairInfo.valueOf(entrance_stair_info),
    entranceStairHeightLevel = entrance_stair_height_level,
    entranceImageUrls = entrance_image_urls,
    entranceImages = entrance_images,
    hasSlope = has_slope,
    hasElevator = has_elevator,
    entranceDoorTypes = entrance_door_types.ifEmpty { null },
    elevatorStairInfo = StairInfo.valueOf(elevator_stair_info),
    elevatorStairHeightLevel = elevator_stair_height_level,
    elevatorImageUrls = elevator_image_urls,
    elevatorImages = elevator_images,
    userId = user_id,
    createdAt = created_at.toInstant(),
    deletedAt = deleted_at?.toInstant(),
)

fun BuildingAccessibility.toPersistenceModel() = Building_accessibility(
    id = id,
    building_id = buildingId,
    entrance_stair_info = entranceStairInfo.name,
    entrance_stair_height_level = entranceStairHeightLevel,
    entrance_image_urls = entranceImageUrls,
    entrance_images = entranceImages,
    has_slope = hasSlope,
    has_elevator = hasElevator,
    entrance_door_types = entranceDoorTypes ?: emptyList(),
    elevator_stair_info = elevatorStairInfo.name,
    elevator_stair_height_level = elevatorStairHeightLevel,
    elevator_image_urls = elevatorImageUrls,
    elevator_images = elevatorImages,
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
    deletedAt = deleted_at?.toInstant(),
)

fun BuildingAccessibilityComment.toPersistenceModel() = Building_accessibility_comment(
    id = id,
    building_id = buildingId,
    user_id = userId,
    comment = comment,
    created_at = createdAt.toOffsetDateTime(),
    updated_at = createdAt.toOffsetDateTime(),
    deleted_at = deletedAt?.toOffsetDateTime(),
)

fun BuildingAccessibilityUpvoteFindById.toDomainModel(): BuildingAccessibilityUpvote {
    val buildingAccessibility = BuildingAccessibility(
        id = id_,
        buildingId = building_id,
        entranceStairInfo = StairInfo.valueOf(entrance_stair_info),
        entranceStairHeightLevel = entrance_stair_height_level,
        entranceImageUrls = entrance_image_urls,
        entranceImages = entrance_images,
        hasSlope = has_slope,
        hasElevator = has_elevator,
        entranceDoorTypes = entrance_door_types,
        elevatorStairInfo = StairInfo.valueOf(elevator_stair_info),
        elevatorStairHeightLevel = elevator_stair_height_level,
        elevatorImageUrls = elevator_image_urls,
        elevatorImages = elevator_images,
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
        entranceStairHeightLevel = entrance_stair_height_level,
        entranceImageUrls = entrance_image_urls,
        entranceImages = entrance_images,
        hasSlope = has_slope,
        hasElevator = has_elevator,
        entranceDoorTypes = entrance_door_types,
        elevatorStairInfo = StairInfo.valueOf(elevator_stair_info),
        elevatorStairHeightLevel = elevator_stair_height_level,
        elevatorImageUrls = elevator_image_urls,
        elevatorImages = elevator_images,
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

fun Place_accessibility.toDomainModel() = PlaceAccessibility(
    id = id,
    placeId = place_id,
    floors = floors.ifEmpty { null },
    isFirstFloor = is_first_floor,
    isStairOnlyOption = is_stair_only_option,
    stairInfo = StairInfo.valueOf(stair_info),
    stairHeightLevel = stair_height_level,
    hasSlope = has_slope,
    entranceDoorTypes = entrance_door_types.ifEmpty { null },
    imageUrls = image_urls,
    images = images,
    userId = user_id,
    createdAt = created_at.toInstant(),
    deletedAt = deleted_at?.toInstant(),
)

fun PlaceAccessibility.toPersistenceModel() = Place_accessibility(
    id = id,
    place_id = placeId,
    floors = floors ?: emptyList(),
    is_first_floor = isFirstFloor,
    is_stair_only_option = isStairOnlyOption,
    stair_info = stairInfo.name,
    stair_height_level = stairHeightLevel,
    has_slope = hasSlope,
    entrance_door_types = entranceDoorTypes ?: emptyList(),
    image_urls = imageUrls,
    images = images,
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
    deletedAt = deleted_at?.toInstant(),
)

fun PlaceAccessibilityComment.toPersistenceModel() = Place_accessibility_comment(
    id = id,
    place_id = placeId,
    user_id = userId,
    comment = comment,
    created_at = createdAt.toOffsetDateTime(),
    updated_at = createdAt.toOffsetDateTime(),
    deleted_at = deletedAt?.toOffsetDateTime(),
)

fun PlaceAccessibilityUpvote.toPersistenceModel() = Place_accessibility_upvote(
    id = id,
    user_id = userId,
    place_accessibility_id = placeAccessibility.id,
    created_at = createdAt.toOffsetDateTime(),
    updated_at = createdAt.toOffsetDateTime(),
    deleted_at = deletedAt?.toOffsetDateTime(),
)

fun FindUpvoteById.toDomainModel(): PlaceAccessibilityUpvote {
    val placeAccessibility = PlaceAccessibility(
        id = id,
        placeId = place_id,
        floors = floors,
        isFirstFloor = is_first_floor,
        isStairOnlyOption = is_stair_only_option,
        stairInfo = StairInfo.valueOf(stair_info),
        stairHeightLevel = stair_height_level,
        hasSlope = has_slope,
        entranceDoorTypes = entrance_door_types,
        imageUrls = image_urls,
        images = images,
        userId = user_id,
        createdAt = created_at.toInstant(),
        deletedAt = deleted_at?.toInstant(),
    )
    return PlaceAccessibilityUpvote(
        id = id,
        userId = user_id,
        placeAccessibility = placeAccessibility,
        createdAt = created_at.toInstant(),
        deletedAt = deleted_at?.toInstant()
    )
}

fun FindUpvoteByUserIdAndPlaceAccessilbityIdAndNotDeleted.toDomainModel(): PlaceAccessibilityUpvote {
    val placeAccessibility = PlaceAccessibility(
        id = id,
        placeId = place_id,
        floors = floors,
        isFirstFloor = is_first_floor,
        isStairOnlyOption = is_stair_only_option,
        stairInfo = StairInfo.valueOf(stair_info),
        stairHeightLevel = stair_height_level,
        hasSlope = has_slope,
        entranceDoorTypes = entrance_door_types,
        imageUrls = image_urls,
        images = images,
        userId = user_id,
        createdAt = created_at.toInstant(),
        deletedAt = deleted_at?.toInstant(),
    )
    return PlaceAccessibilityUpvote(
        id = id,
        userId = user_id,
        placeAccessibility = placeAccessibility,
        createdAt = created_at.toInstant(),
        deletedAt = deleted_at?.toInstant()
    )
}


fun AccessibilityRank.toPersistenceModel() = Accessibility_rank(
    id = id,
    user_id = userId,
    conquered_count = conqueredCount,
    rank = rank,
    created_at = createdAt.toOffsetDateTime(),
    updated_at = updatedAt.toOffsetDateTime(),
)

fun Accessibility_rank.toDomainModel() = AccessibilityRank(
    id = id,
    userId = user_id,
    conqueredCount = conquered_count,
    rank = rank,
    createdAt = created_at.toInstant(),
    updatedAt = updated_at.toInstant(),
)

fun AccessibilityAllowedRegion.toPersistenceModel() = Accessibility_allowed_region(
    id = id,
    name = name,
    boundary_vertices = boundaryVertices,
    created_at = createdAt.toOffsetDateTime(),
    updated_at = updatedAt.toOffsetDateTime(),
)

fun Accessibility_allowed_region.toDomainModel() = AccessibilityAllowedRegion(
    id = id,
    name = name,
    boundaryVertices = boundary_vertices,
    createdAt = created_at.toInstant(),
    updatedAt = updated_at.toInstant(),
)
