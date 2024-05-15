package club.staircrusher.quest.application.port.`in`

import club.staircrusher.place.application.port.`in`.PlaceApplicationService
import club.staircrusher.quest.application.port.out.persistence.ClubQuestRepository
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class ClubQuestMigrateToPlaceUseCase(
    private val transactionManager: TransactionManager,
    private val clubQuestRepository: ClubQuestRepository,
    private val placeApplicationService: PlaceApplicationService,
) {
    fun handle() {
        val clubQuestIds = clubQuestRepository.findAllOrderByCreatedAtDesc()
            .map { it.id }
        clubQuestIds.forEach { clubQuestId ->
            transactionManager.doInTransaction {
                val clubQuest = clubQuestRepository.findById(clubQuestId)
                clubQuest.targetBuildings
                    .flatMap { it.places }
                    .forEach forEachInner@{ targetPlace ->
                        if (!targetPlace.isClosed && !targetPlace.isNotAccessible) {
                            return@forEachInner
                        }
                        placeApplicationService.setIsClosed(targetPlace.placeId, targetPlace.isClosed)
                        placeApplicationService.setIsNotAccessible(targetPlace.placeId, targetPlace.isNotAccessible)
                    }
            }
        }
    }
}
