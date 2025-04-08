package club.staircrusher.quest.application.port.`in`

import club.staircrusher.place.application.port.`in`.place.PlaceApplicationService
import club.staircrusher.quest.application.port.out.persistence.ClubQuestRepository
import club.staircrusher.quest.application.port.out.web.ConqueredPlaceService
import club.staircrusher.quest.domain.model.ClubQuest
import club.staircrusher.stdlib.di.annotation.Component

@Component
class ClubQuestDtoAggregator(
    private val clubQuestRepository: ClubQuestRepository,
    private val conqueredPlaceService: ConqueredPlaceService,
    private val placeApplicationService: PlaceApplicationService,
) {
    fun withDtoInfo(clubQuestId: String): ClubQuestWithDtoInfo {
        val clubQuest = clubQuestRepository.findById(clubQuestId).get()
        return withDtoInfo(clubQuest)
    }

    fun withDtoInfo(clubQuest: ClubQuest): ClubQuestWithDtoInfo {
        val placeIdsInClubQuest = clubQuest.targetBuildings.flatMap { it.places }.map { it.placeId }

        return ClubQuestWithDtoInfo(
            quest = clubQuest,
            conqueredPlaceIds = conqueredPlaceService.getConqueredPlaceIds(clubQuest),
            placeById = placeApplicationService.findAllByIds(placeIdsInClubQuest)
                .associateBy { it.id },
        )
    }
}
