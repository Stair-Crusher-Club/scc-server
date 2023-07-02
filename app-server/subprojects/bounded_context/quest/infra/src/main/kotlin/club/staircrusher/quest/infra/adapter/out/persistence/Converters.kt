package club.staircrusher.quest.infra.adapter.out.persistence

import club.staircrusher.infra.persistence.sqldelight.migration.Club_quest
import club.staircrusher.quest.domain.model.ClubQuest
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.time.toOffsetDateTime

fun ClubQuest.toPersistenceModel() = Club_quest(
    id = id,
    name = name,
    quest_center_location_x = questCenterLocation.lng,
    quest_center_location_y = questCenterLocation.lat,
    target_buildings = targetBuildings,
    created_at = createdAt.toOffsetDateTime(),
    updated_at = updatedAt.toOffsetDateTime(),
)

fun Club_quest.toDomainModel() = ClubQuest(
    id = id,
    name = name,
    questCenterLocation = Location(lng = quest_center_location_x, lat = quest_center_location_y),
    targetBuildings = target_buildings,
    createdAt = created_at.toInstant(),
    updatedAt = updated_at.toInstant(),
)
