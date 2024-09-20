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
            )
        }

        transactionManager.doInTransaction {
            closedPlaceCandidateRepository.saveAll(closedPlaceCandidates)
        }
    }

    companion object {
        private const val SEARCH_RADIUS = 10
    }
}
