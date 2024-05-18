package club.staircrusher.quest.application.port.`in`

import club.staircrusher.place.application.port.`in`.PlaceApplicationService
import club.staircrusher.quest.application.port.out.persistence.ClubQuestRepository
import club.staircrusher.quest.application.port.out.web.ClubQuestTargetPlacesSearcher
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.util.concurrent.Executors

@Component
class CrossValidateClubQuestPlacesUseCase(
    private val transactionManager: TransactionManager,
    private val clubQuestRepository: ClubQuestRepository,
    private val clubQuestTargetPlacesSearcher: ClubQuestTargetPlacesSearcher,
    private val placeApplicationService: PlaceApplicationService,
) {
    @Suppress("MagicNumber")
    private val taskExecutor = Executors.newCachedThreadPool()

    fun handle(questId: String) {
        taskExecutor.execute {
            logger.info("[$questId] Start CrossValidateClubQuestPlaces")
            try {
                val places = transactionManager.doInTransaction {
                    val placeIdsInQuest = clubQuestRepository.findById(questId).targetBuildings.flatMap { it.places }.map { it.placeId }
                    logger.info("[$questId] placeIdsInQuest: $placeIdsInQuest")
                    placeApplicationService.findAllByIds(placeIdsInQuest)
                }

                val closedPlaceIds = runBlocking {
                    val isNotClosedList = clubQuestTargetPlacesSearcher.crossValidatePlaces(places)
                    places.zip(isNotClosedList)
                        .filter { (_, isNotClosed) -> !isNotClosed }
                        .map { (place, _) -> place.id }
                }
                logger.info("[$questId] closedPlaceIds: $closedPlaceIds")

                transactionManager.doInTransaction {
                    closedPlaceIds.forEach { closedPlaceId ->
                        try {
                            placeApplicationService.setIsClosed(closedPlaceId, isClosed = true)
                        } catch (e: IllegalArgumentException) {
                            // ignore
                        }
                    }
                }
            } catch (t: Throwable) {
                logger.error(t) { "[$questId] CrossValidateClubQuestPlaces failed" }
            }
            logger.info("[$questId] Finish CrossValidateClubQuestPlaces")
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
