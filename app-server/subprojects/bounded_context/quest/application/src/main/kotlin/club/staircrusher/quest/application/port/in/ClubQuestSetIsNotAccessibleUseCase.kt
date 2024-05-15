package club.staircrusher.quest.application.port.`in`

import club.staircrusher.place.application.port.`in`.PlaceApplicationService
import club.staircrusher.quest.application.port.out.persistence.ClubQuestRepository
import club.staircrusher.quest.application.port.out.persistence.ClubQuestTargetPlaceRepository
import club.staircrusher.quest.application.port.out.web.ConqueredPlaceService
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class ClubQuestSetIsNotAccessibleUseCase(
    private val transactionManager: TransactionManager,
    private val clubQuestRepository: ClubQuestRepository,
    private val conqueredPlaceService: ConqueredPlaceService,
    private val placeApplicationService: PlaceApplicationService,
    private val clubQuestTargetPlaceRepository: ClubQuestTargetPlaceRepository,
) {
    fun handle(
        clubQuestId: String,
        buildingId: String,
        placeId: String,
        isNotAccessible: Boolean,
    ): ClubQuestWithDtoInfo = transactionManager.doInTransaction {
        val clubQuest = clubQuestRepository.findById(clubQuestId)
        val targetPlace = clubQuest.setIsNotAccessible(buildingId, placeId, isNotAccessible)
        if (targetPlace != null) {
            clubQuestTargetPlaceRepository.save(targetPlace)

            // dual write
            try {
                placeApplicationService.setIsNotAccessible(targetPlace.placeId, isNotAccessible)
            } catch (e: IllegalArgumentException) {
                // ignore
            }
        }

        ClubQuestWithDtoInfo(
            quest = clubQuest,
            conqueredPlaceIds = conqueredPlaceService.getConqueredPlaceIds(clubQuest),
        )
    }
}
