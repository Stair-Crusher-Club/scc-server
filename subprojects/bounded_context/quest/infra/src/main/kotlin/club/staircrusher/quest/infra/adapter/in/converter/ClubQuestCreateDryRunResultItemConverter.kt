package club.staircrusher.quest.infra.adapter.`in`.converter

import club.staircrusher.admin.api.dto.ClubQuestCreateDryRunResultItemDTO
import club.staircrusher.quest.domain.model.ClubQuestCreateDryRunResultItem

object ClubQuestCreateDryRunResultItemConverter {
    fun convertToModel(dto: ClubQuestCreateDryRunResultItemDTO): ClubQuestCreateDryRunResultItem {
        return ClubQuestCreateDryRunResultItem(
            questCenterLocation = LocationConverter.convertToModel(dto.questCenterLocation),
            targetPlaces = dto.targetPlaces.map(ClubQuestTargetPlaceConverter::convertToModel),
        )
    }

    fun convertToDTO(model: ClubQuestCreateDryRunResultItem): ClubQuestCreateDryRunResultItemDTO {
        return ClubQuestCreateDryRunResultItemDTO(
            questCenterLocation = LocationConverter.convertToDTO(model.questCenterLocation),
            targetPlaces = model.targetPlaces.map(ClubQuestTargetPlaceConverter::convertToDTO)
        )
    }
}
