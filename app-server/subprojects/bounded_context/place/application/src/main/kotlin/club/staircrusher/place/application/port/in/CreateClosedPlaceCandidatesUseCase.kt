package club.staircrusher.place.application.port.`in`

import club.staircrusher.place.application.port.out.persistence.ClosedPlaceCandidateRepository
import club.staircrusher.place.application.port.out.web.OpenDataService
import club.staircrusher.place.domain.model.ClosedPlaceCandidate
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.stdlib.time.toInstant
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CreateClosedPlaceCandidatesUseCase(
    private val transactionManager: TransactionManager,
    private val closedPlaceCandidateRepository: ClosedPlaceCandidateRepository,
    private val placeApplicationService: PlaceApplicationService,
    private val openDataService: OpenDataService,
) {
    private val logger = KotlinLogging.logger {}

    fun handle() {
        val closedPlacesFromOpenData = openDataService.getClosedPlaces()

        val closedPlaceCandidates = closedPlacesFromOpenData.mapNotNull { closedPlace ->
            val nearbyPlaces = placeApplicationService.searchPlacesInCircle(closedPlace.location, SEARCH_RADIUS)
            if (nearbyPlaces.isEmpty()) return@mapNotNull null

            val placeToSimilarity = nearbyPlaces
                .associateWith { StringSimilarityComparator.getSimilarity(it.name, closedPlace.name) }
            val similarPlace = placeToSimilarity.minByOrNull { it.value }
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
                closedAt = closedPlace.closedDate.toInstant(),
            )
        }

        transactionManager.doInTransaction {
            val (placeIds ,externalIds) = closedPlaceCandidates.map { it.placeId to it.externalId }.unzip()

            // 혹시 다른 externalId 지만 similarity 알고리즘의 부정확성 때문에 같은 장소로 판단되는지 확인하지 못해서 일단 보류합니다
            // val alreadyExistingPlaceIds = closedPlaceCandidateRepository.findByPlaceIdIn(placeIds).map { it.placeId }
            val alreadyExistingExternalIds = closedPlaceCandidateRepository.findByExternalIdIn(externalIds).map { it.externalId }
            val filteredCandidates = closedPlaceCandidates
                .filter { it.externalId !in alreadyExistingExternalIds }

            closedPlaceCandidateRepository.saveAll(filteredCandidates)
        }
    }

    companion object {
        private const val SEARCH_RADIUS = 30
    }
}
