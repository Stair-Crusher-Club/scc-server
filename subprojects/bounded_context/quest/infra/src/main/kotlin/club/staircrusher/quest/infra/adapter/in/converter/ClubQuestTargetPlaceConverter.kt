package club.staircrusher.quest.infra.adapter.`in`.converter

import club.staircrusher.admin.api.dto.ClubQuestTargetPlaceDTO
import club.staircrusher.quest.domain.model.ClubQuestTargetPlace

object ClubQuestTargetPlaceConverter {
    fun convertToModel(dto: ClubQuestTargetPlaceDTO): ClubQuestTargetPlace {
        return ClubQuestTargetPlace(
            name = dto.name,
            location = LocationConverter.convertToModel(dto.location),
            placeId = dto.placeId,
        )
    }

    fun convertToDTO(model: ClubQuestTargetPlace): ClubQuestTargetPlaceDTO {
        return ClubQuestTargetPlaceDTO(
            name = model.name,
            location = LocationConverter.convertToDTO(model.location),
            placeId = model.placeId,
        )
    }
}
