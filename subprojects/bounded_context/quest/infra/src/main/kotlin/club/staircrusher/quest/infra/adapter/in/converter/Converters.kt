package club.staircrusher.quest.infra.adapter.`in`.converter

import club.staircrusher.admin.api.dto.ClubQuestCreateDryRunResultItemDTO
import club.staircrusher.admin.api.dto.ClubQuestDTO
import club.staircrusher.admin.api.dto.ClubQuestTargetBuildingDTO
import club.staircrusher.admin.api.dto.ClubQuestTargetPlaceDTO
import club.staircrusher.quest.application.port.`in`.ClubQuestWithDtoInfo
import club.staircrusher.quest.domain.model.ClubQuestCreateDryRunResultItem
import club.staircrusher.quest.domain.model.ClubQuestTargetBuilding
import club.staircrusher.quest.domain.model.ClubQuestTargetPlace


fun ClubQuestCreateDryRunResultItemDTO.toModel() = ClubQuestCreateDryRunResultItem(
    questCenterLocation = questCenterLocation.toModel(),
    targetBuildings = targetBuildings.map { it.toModel() },
)

fun ClubQuestCreateDryRunResultItem.toDTO(conqueredPlaceIds: Set<String>) =
    ClubQuestCreateDryRunResultItemDTO(
        questCenterLocation = questCenterLocation.toDTO(),
        targetBuildings = targetBuildings.map { it.toDTO(conqueredPlaceIds) }
    )

fun ClubQuestWithDtoInfo.toDTO() = ClubQuestDTO(
    id = quest.id,
    name = quest.name,
    buildings = quest.targetBuildings.map { it.toDTO(conqueredPlaceIds) }
)

fun ClubQuestTargetBuildingDTO.toModel() = ClubQuestTargetBuilding(
    buildingId = buildingId,
    name = name,
    location = location.toModel(),
    places = places.map { it.toModel() },
)

fun ClubQuestTargetBuilding.toDTO(conqueredPlaceIds: Set<String>) = ClubQuestTargetBuildingDTO(
    buildingId = buildingId,
    name = name,
    location = location.toDTO(),
    places = places.map { it.toDTO(isConquered = it.placeId in conqueredPlaceIds) },
)

fun ClubQuestTargetPlaceDTO.toModel() = ClubQuestTargetPlace(
    name = name,
    location = location.toModel(),
    placeId = placeId,
    buildingId = buildingId,
    isClosed = isClosed,
    isNotAccessible = isNotAccessible,
)

fun ClubQuestTargetPlace.toDTO(isConquered: Boolean): ClubQuestTargetPlaceDTO {
    return ClubQuestTargetPlaceDTO(
        name = name,
        location = location.toDTO(),
        placeId = placeId,
        buildingId = buildingId,
        isConquered = isConquered,
        isClosed = isClosed,
        isNotAccessible = isNotAccessible,
    )
}
