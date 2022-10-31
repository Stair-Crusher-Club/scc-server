package club.staircrusher.quest.infra.adapter.out.persistence

import club.staircrusher.infra.persistence.sqldelight.migration.Club_quest
import club.staircrusher.quest.domain.model.ClubQuest
import club.staircrusher.quest.domain.model.ClubQuestTargetBuilding
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.time.toOffsetDateTime
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

private val objectMapper = jacksonObjectMapper()
    .findAndRegisterModules()
    .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

private fun List<ClubQuestTargetBuilding>.toPersistenceModel() = objectMapper.writeValueAsString(this)
private fun String.toDomainModel() = objectMapper.readValue(this, object : TypeReference<List<ClubQuestTargetBuilding>>() {})

fun ClubQuest.toPersistenceModel() = Club_quest(
    id = id,
    name = name,
    quest_center_location_x = questCenterLocation.lng,
    quest_center_location_y = questCenterLocation.lat,
    target_buildings = targetBuildings.toPersistenceModel(),
    created_at = createdAt.toOffsetDateTime(),
    updated_at = updatedAt.toOffsetDateTime(),
)

fun Club_quest.toDomainModel() = ClubQuest(
    id = id,
    name = name,
    questCenterLocation = Location(lng = quest_center_location_x, lat = quest_center_location_y),
    targetBuildings = target_buildings.toDomainModel(),
    createdAt = created_at.toInstant(),
    updatedAt = updated_at.toInstant(),
)
