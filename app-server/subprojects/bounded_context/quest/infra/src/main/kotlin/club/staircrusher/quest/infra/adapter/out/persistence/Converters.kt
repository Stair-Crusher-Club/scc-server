package club.staircrusher.quest.infra.adapter.out.persistence

import club.staircrusher.infra.persistence.sqldelight.migration.Club_quest
import club.staircrusher.infra.persistence.sqldelight.migration.Club_quest_target_building
import club.staircrusher.infra.persistence.sqldelight.migration.Club_quest_target_place
import club.staircrusher.quest.domain.model.ClubQuest
import club.staircrusher.quest.domain.model.ClubQuestTargetBuilding
import club.staircrusher.quest.domain.model.ClubQuestTargetPlace
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.time.toOffsetDateTime

fun ClubQuest.toPersistenceModel() = Club_quest(
    id = id,
    name = name,
    purpose_type = purposeType,
    start_at = startAt.toOffsetDateTime(),
    end_at = endAt.toOffsetDateTime(),
    quest_center_location_x = questCenterLocation.lng,
    quest_center_location_y = questCenterLocation.lat,
    shortened_admin_url = shortenedAdminUrl,
    created_at = createdAt.toOffsetDateTime(),
    updated_at = updatedAt.toOffsetDateTime(),
)

fun Club_quest.toDomainModel(targetBuildings: List<ClubQuestTargetBuilding>) = ClubQuest(
    id = id,
    name = name,
    purposeType = purpose_type,
    startAt = start_at.toInstant(),
    endAt = end_at.toInstant(),
    questCenterLocation = Location(lng = quest_center_location_x, lat = quest_center_location_y),
    targetBuildings = targetBuildings.sortedBy { it.name.padStart(5, '0') },
    shortenedAdminUrl = shortened_admin_url,
    createdAt = created_at.toInstant(),
    updatedAt = updated_at.toInstant(),
)

fun ClubQuestTargetBuilding.toPersistenceModel() = Club_quest_target_building(
    id = id,
    club_quest_id = clubQuestId,
    building_id = buildingId,
    name = name,
    location_x = location.lng,
    location_y = location.lat,
    created_at = createdAt.toOffsetDateTime(),
    updated_at = updatedAt.toOffsetDateTime(),
)

fun Club_quest_target_building.toDomainModel(places: List<ClubQuestTargetPlace>) = ClubQuestTargetBuilding(
    id = id,
    clubQuestId = club_quest_id,
    buildingId = building_id,
    name = name,
    location = Location(lng = location_x, lat = location_y),
    places = places,
    createdAt = created_at.toInstant(),
    updatedAt = updated_at.toInstant(),
)

fun ClubQuestTargetPlace.toPersistenceModel() = Club_quest_target_place(
    id = id,
    club_quest_id = clubQuestId,
    target_building_id = targetBuildingId,
    building_id = buildingId,
    place_id = placeId,
    name = name,
    location_x = location.lng,
    location_y = location.lat,
    is_closed_expected = isClosedExpected,
    created_at = createdAt.toOffsetDateTime(),
    updated_at = updatedAt.toOffsetDateTime(),
)

fun Club_quest_target_place.toDomainModel() = ClubQuestTargetPlace(
    id = id,
    clubQuestId = club_quest_id,
    targetBuildingId = target_building_id,
    buildingId = building_id,
    placeId = place_id,
    name = name,
    location = Location(lng = location_x, lat = location_y),
    isClosedExpected = is_closed_expected,
    createdAt = created_at.toInstant(),
    updatedAt = updated_at.toInstant(),
)
