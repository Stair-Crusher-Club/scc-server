package club.staircrusher.quest.application.port.`in`

import club.staircrusher.place.application.port.`in`.PlaceService
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
    private val placeService: PlaceService,
) {
    @Suppress("MagicNumber")
    private val taskExecutor = Executors.newFixedThreadPool(4) // 4인 이유는... 그냥 author 마음임.

    fun handle(questId: String) {
        taskExecutor.execute {
            logger.info("[$questId] Start CrossValidateClubQuestPlaces")
            try {
                val placeIdsInQuest = transactionManager.doInTransaction {
                    clubQuestRepository.findById(questId).targetBuildings.flatMap { it.places }.map { it.placeId }
                }
                logger.info("[$questId] placeIdsInQuest: $placeIdsInQuest")

                val places = placeService.findAllByIds(placeIdsInQuest)
                val closedPlaceIds = runBlocking {
                    val isNotClosedList = clubQuestTargetPlacesSearcher.crossValidatePlaces(places)
                    places.zip(isNotClosedList)
                        .filterNot { (_, isNotClosed) -> isNotClosed }
                        .map { (place, _) -> place.id }
                }
                logger.info("[$questId] closedPlaceIds: $closedPlaceIds")

                transactionManager.doInTransaction {
                    val quest = clubQuestRepository.findById(questId)
                    val placeById = places.associateBy { it.id }
                    closedPlaceIds.forEach { closedPlaceId ->
                        val closedPlace = placeById[closedPlaceId]!!
                        quest.setIsClosed(closedPlace.building.id, closedPlace.id, true)
                    }
                    clubQuestRepository.save(quest)
                }
            } catch (t: Throwable) {
                logger.error("[$questId] CrossValidateClubQuestPlaces failed", t)
            }
            logger.info("[$questId] Finish CrossValidateClubQuestPlaces")
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
