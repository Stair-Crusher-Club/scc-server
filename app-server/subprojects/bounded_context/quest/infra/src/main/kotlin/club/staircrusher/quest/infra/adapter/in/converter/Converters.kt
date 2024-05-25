package club.staircrusher.quest.infra.adapter.`in`.converter

import club.staircrusher.admin_api.converter.toDTO
import club.staircrusher.admin_api.converter.toModel
import club.staircrusher.admin_api.spec.dto.ClubQuestCreateDryRunResultItemDTO
import club.staircrusher.admin_api.spec.dto.ClubQuestDTO
import club.staircrusher.admin_api.spec.dto.ClubQuestTargetBuildingDTO
import club.staircrusher.admin_api.spec.dto.ClubQuestTargetPlaceDTO
import club.staircrusher.place.domain.model.Place
import club.staircrusher.quest.application.port.`in`.ClubQuestWithDtoInfo
import club.staircrusher.quest.domain.model.ClubQuestCreateDryRunResultItem
import club.staircrusher.quest.domain.model.ClubQuestTargetBuilding
import club.staircrusher.quest.domain.model.DryRunnedClubQuestTargetBuilding
import club.staircrusher.quest.domain.model.ClubQuestTargetPlace
import club.staircrusher.quest.domain.model.DryRunnedClubQuestTargetPlace


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

fun DryRunnedClubQuestTargetBuilding.toDTO(conqueredPlaceIds: Set<String>) = ClubQuestTargetBuildingDTO(
    buildingId = buildingId,
    name = name,
    location = location.toDTO(),
    places = places.map {
        it.toDTO(
            isConquered = it.placeId in conqueredPlaceIds,
        )
    },
)

fun DryRunnedClubQuestTargetPlace.toDTO(isConquered: Boolean): ClubQuestTargetPlaceDTO {
    return ClubQuestTargetPlaceDTO(
        name = name,
        location = location.toDTO(),
        placeId = placeId,
        buildingId = buildingId,
        isConquered = isConquered,
        isClosed = false, // DryRunnedClubQuestTargetPlace에는 필요 없는 필드이지만, 프론트엔드 하위호환을 위해 남겨둔다.
        isClosedExpected = false, // DryRunnedClubQuestTargetPlace에는 필요 없는 필드이지만, 프론트엔드 하위호환을 위해 남겨둔다.
        isNotAccessible = false, // DryRunnedClubQuestTargetPlace에는 필요 없는 필드이지만, 프론트엔드 하위호환을 위해 남겨둔다.
    )
}

fun ClubQuestWithDtoInfo.toDTO() = ClubQuestDTO(
    id = quest.id,
    name = quest.name,
    buildings = quest.targetBuildings.map { it.toDTO(conqueredPlaceIds, placeById) }
)

fun ClubQuestTargetBuildingDTO.toModel() = DryRunnedClubQuestTargetBuilding(
    buildingId = buildingId,
    name = name,
    location = location.toModel(),
    places = places.map { it.toModel() },
)

fun ClubQuestTargetBuilding.toDTO(
    conqueredPlaceIds: Set<String>,
    placeById: Map<String, Place>,
) = ClubQuestTargetBuildingDTO(
    buildingId = buildingId,
    name = name,
    location = location.toDTO(),
    places = places.map {
        it.toDTO(
            isConquered = it.placeId in conqueredPlaceIds,
            isClosed = placeById[it.placeId]!!.isClosed,
            isNotAccessible = placeById[it.placeId]!!.isNotAccessible,
        )
    },
)

fun ClubQuestTargetPlaceDTO.toModel() = DryRunnedClubQuestTargetPlace(
    name = name,
    buildingId = buildingId,
    placeId = placeId,
    location = location.toModel(),
)

fun ClubQuestTargetPlace.toDTO(
    isConquered: Boolean,
    isClosed: Boolean,
    isNotAccessible: Boolean,
): ClubQuestTargetPlaceDTO {
    return ClubQuestTargetPlaceDTO(
        name = name,
        location = location.toDTO(),
        placeId = placeId,
        buildingId = buildingId,
        isConquered = isConquered,
        isClosedExpected = isClosedExpected,
        isClosed = isClosed,
        isNotAccessible = isNotAccessible,
    )
}
