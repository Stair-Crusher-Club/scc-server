package club.staircrusher.quest.infra.adapter.`in`.converter

import club.staircrusher.admin_api.converter.toDTO
import club.staircrusher.admin_api.converter.toModel
import club.staircrusher.admin_api.spec.dto.ClubQuestCreateDryRunResultItemDTO
import club.staircrusher.admin_api.spec.dto.ClubQuestDTO
import club.staircrusher.admin_api.spec.dto.ClubQuestTargetBuildingDTO
import club.staircrusher.admin_api.spec.dto.ClubQuestTargetPlaceDTO
import club.staircrusher.quest.application.port.`in`.ClubQuestWithDtoInfo
import club.staircrusher.quest.domain.model.ClubQuestCreateDryRunResultItem
import club.staircrusher.quest.domain.model.ClubQuestTargetBuilding
import club.staircrusher.quest.domain.model.ClubQuestTargetBuildingVO
import club.staircrusher.quest.domain.model.ClubQuestTargetPlace
import club.staircrusher.quest.domain.model.ClubQuestTargetPlaceVO


fun ClubQuestCreateDryRunResultItemDTO.toModel() = ClubQuestCreateDryRunResultItem(
    questNamePostfix = questNamePostfix,
    questCenterLocation = questCenterLocation.toModel(),
    targetBuildings = targetBuildings.map { it.toModel() },
)

fun ClubQuestCreateDryRunResultItem.toDTO(conqueredPlaceIds: Set<String>) =
    ClubQuestCreateDryRunResultItemDTO(
        questNamePostfix = questNamePostfix,
        questCenterLocation = questCenterLocation.toDTO(),
        targetBuildings = targetBuildings.map { it.toDTO(conqueredPlaceIds) }
    )

fun ClubQuestTargetBuildingVO.toDTO(conqueredPlaceIds: Set<String>) = ClubQuestTargetBuildingDTO(
    buildingId = buildingId,
    name = name,
    location = location.toDTO(),
    places = places.map { it.toDTO(isConquered = it.placeId in conqueredPlaceIds) },
)

fun ClubQuestTargetPlaceVO.toDTO(isConquered: Boolean): ClubQuestTargetPlaceDTO {
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

fun ClubQuestWithDtoInfo.toDTO() = ClubQuestDTO(
    id = quest.id,
    name = quest.name,
    buildings = quest.targetBuildings.map { it.toDTO(conqueredPlaceIds) }
)

fun ClubQuestTargetBuildingDTO.toModel() = ClubQuestTargetBuildingVO(
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

fun ClubQuestTargetPlaceDTO.toModel() = ClubQuestTargetPlaceVO(
    name = name,
    buildingId = buildingId,
    placeId = placeId,
    location = location.toModel(),
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
