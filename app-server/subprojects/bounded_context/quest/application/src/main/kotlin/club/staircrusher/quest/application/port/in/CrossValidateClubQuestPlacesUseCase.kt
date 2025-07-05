package club.staircrusher.quest.application.port.`in`

import club.staircrusher.place.application.port.`in`.place.PlaceApplicationService
import club.staircrusher.place.application.port.`in`.place.PlaceCrawler
import club.staircrusher.quest.application.port.out.persistence.ClubQuestRepository
import club.staircrusher.quest.application.port.out.persistence.ClubQuestTargetPlaceRepository
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.util.concurrent.Executors

@Component
class CrossValidateClubQuestPlacesUseCase(
    private val transactionManager: TransactionManager,
    private val clubQuestRepository: ClubQuestRepository,
    private val placeCrawler: PlaceCrawler,
    private val placeApplicationService: PlaceApplicationService,
    private val clubQuestTargetPlaceRepository: ClubQuestTargetPlaceRepository,
) {
    @Suppress("MagicNumber")
    private val taskExecutor = Executors.newCachedThreadPool()

    fun handleAsync(questId: String) {
        taskExecutor.execute {
            doHandle(questId)
        }
    }

    fun handle(questId: String) {
        doHandle(questId)
    }

    private fun doHandle(questId: String) {
        logger.info("[$questId] Start CrossValidateClubQuestPlaces")
        try {
            val places = transactionManager.doInTransaction {
                val placeIdsInQuest = clubQuestRepository.findById(questId).get().targetBuildings.flatMap { it.places }.map { it.placeId }
                logger.info("[$questId] placeIdsInQuest: $placeIdsInQuest")
                placeApplicationService.findAllByIds(placeIdsInQuest)
            }

            val closedExpectedPlaceIds = runBlocking {
                val isNotClosedList = placeCrawler.crossValidatePlaces(places)
                places.zip(isNotClosedList)
                    .filter { (_, isNotClosed) -> !isNotClosed }
                    .map { (place, _) -> place.id }
            }
            logger.info("[$questId] closedExpectedPlaceIds: $closedExpectedPlaceIds")

            transactionManager.doInTransaction {
                val targetPlaces = clubQuestTargetPlaceRepository.findByClubQuestIdAndPlaceIdIn(
                    clubQuestId = questId,
                    placeIds = closedExpectedPlaceIds,
                )
                targetPlaces.forEach {
                    it.expectToBeClosed()
                }
                clubQuestTargetPlaceRepository.saveAll(targetPlaces)
            }
        } catch (t: Throwable) {
            logger.error(t) { "[$questId] CrossValidateClubQuestPlaces failed" }
        }
        logger.info("[$questId] Finish CrossValidateClubQuestPlaces")
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
