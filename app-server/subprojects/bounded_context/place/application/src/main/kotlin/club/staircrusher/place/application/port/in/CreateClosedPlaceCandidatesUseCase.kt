package club.staircrusher.place.application.port.`in`

import club.staircrusher.place.application.port.out.persistence.ClosedPlaceCandidateRepository
import club.staircrusher.place.application.port.out.web.OpenDataService
import club.staircrusher.place.domain.model.ClosedPlaceCandidate
import club.staircrusher.stdlib.persistence.TransactionManager
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CreateClosedPlaceCandidatesUseCase(
    private val transactionManager: TransactionManager,
    private val closedPlaceCandidateRepository: ClosedPlaceCandidateRepository,
    private val placeApplicationService: PlaceApplicationService,
    private val openDataService: OpenDataService,
) {
    fun handle() {
        val closedPlacesFromOpenData = openDataService.getClosedPlaces()

        val closedPlaceCandidates = closedPlacesFromOpenData.mapNotNull { closedPlace ->
            val nearbyPlaces = placeApplicationService.searchPlacesInCircle(closedPlace.location, SEARCH_RADIUS)
            if (nearbyPlaces.isEmpty()) return@mapNotNull null

            val similarPlace = nearbyPlaces
                .minByOrNull { StringSimilarityComparator.getSimilarity(it.name, closedPlace.name) }
                ?: return@mapNotNull null

            ClosedPlaceCandidate(
                id = UUID.randomUUID().toString(),
                placeId = similarPlace.id,
                externalId = closedPlace.externalId,
            )
        }

        transactionManager.doInTransaction {
            val externalIds = closedPlaceCandidates.map { it.externalId }
            val alreadyExistingExternalIds = closedPlaceCandidateRepository.findByExternalIdIn(externalIds).map { it.externalId }
            val filteredCandidates = closedPlaceCandidates.filter { it.externalId !in alreadyExistingExternalIds }

            closedPlaceCandidateRepository.saveAll(filteredCandidates)
        }
    }

    companion object {
        private const val SEARCH_RADIUS = 10
    }
}
