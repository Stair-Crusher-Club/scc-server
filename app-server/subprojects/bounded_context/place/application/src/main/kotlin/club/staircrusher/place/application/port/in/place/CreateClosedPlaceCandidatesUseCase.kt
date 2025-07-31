package club.staircrusher.place.application.port.`in`.place

import club.staircrusher.place.application.port.out.place.persistence.ClosedPlaceCandidateRepository
import club.staircrusher.place.application.port.out.place.web.OpenDataService
import club.staircrusher.place.domain.model.place.ClosedPlaceCandidate
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.stdlib.time.toStartOfDay
import club.staircrusher.stdlib.util.string.getSimilarityWith
import com.google.common.util.concurrent.RateLimiter
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.Executors

@Service
class CreateClosedPlaceCandidatesUseCase(
    private val transactionManager: TransactionManager,
    private val closedPlaceCandidateRepository: ClosedPlaceCandidateRepository,
    private val placeApplicationService: PlaceApplicationService,
    private val openDataService: OpenDataService,
) {
    private val executor = Executors.newSingleThreadExecutor()
    private val logger = KotlinLogging.logger {}
    @Suppress("UnstableApiUsage", "MagicNumber")
    private val rateLimiter = RateLimiter.create(5.0)

    fun handle() {
        logger.info { "[CreateClosedPlaceCandidates] Starting to create closed place candidates" }
        executor.submit {
            doHandle()
        }
    }

    private fun doHandle() {
        val closedPlacesFromOpenData = openDataService.getClosedPlaces()

        val closedPlaceCandidates = closedPlacesFromOpenData.mapNotNull { closedPlace ->
            rateLimiter.acquire()
            val nearbyPlaces = transactionManager.doInTransaction(isReadOnly = true) {
                placeApplicationService.searchPlacesInCircle(closedPlace.location, SEARCH_RADIUS)
            }

            if (nearbyPlaces.isEmpty()) return@mapNotNull null
            val placeToSimilarity = nearbyPlaces
                .associateWith { it.name.getSimilarityWith(closedPlace.name) }
            val similarPlace = placeToSimilarity
                .filter { it.value < SIMILARITY_THRESHOLD }
                .minByOrNull { it.value }
                ?.also {
                    logger.info("[CreateClosedPlaceCandidates] most similar place for ${closedPlace.name} is ${it.key.name} with similarity of ${it.value}")
                }
                ?.key
                ?: return@mapNotNull null

            ClosedPlaceCandidate(
                id = UUID.randomUUID().toString(),
                placeId = similarPlace.id,
                externalId = closedPlace.externalId,
                originalName = closedPlace.name,
                originalAddress = closedPlace.address,
                closedAt = closedPlace.closedDate.toStartOfDay(),
            )
        }

        transactionManager.doInTransaction {
            val externalIds = closedPlaceCandidates.map { it.externalId }
            val alreadyExistingExternalIds = closedPlaceCandidateRepository.findByExternalIdIn(externalIds).map { it.externalId }
            val filteredCandidates = closedPlaceCandidates
                .filter { it.externalId !in alreadyExistingExternalIds }

            closedPlaceCandidateRepository.saveAll(filteredCandidates)
        }

        logger.info { "[CreateClosedPlaceCandidates] Job completed" }
    }

    companion object {
        private const val SEARCH_RADIUS = 30
        private const val SIMILARITY_THRESHOLD = 0.2
    }
}
