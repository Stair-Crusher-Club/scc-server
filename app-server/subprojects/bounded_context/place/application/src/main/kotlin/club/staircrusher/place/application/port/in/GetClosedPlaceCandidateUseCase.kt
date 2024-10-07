package club.staircrusher.place.application.port.`in`

import club.staircrusher.place.application.port.out.persistence.ClosedPlaceCandidateRepository
import club.staircrusher.place.application.port.out.persistence.PlaceRepository
import club.staircrusher.place.application.result.NamedClosedPlaceCandidate
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager
import org.springframework.data.repository.findByIdOrNull

@Component
class GetClosedPlaceCandidateUseCase(
    private val transactionManager: TransactionManager,
    private val closedPlaceCandidateRepository: ClosedPlaceCandidateRepository,
    private val placeRepository: PlaceRepository,
) {
    fun handle(candidateId: String) = transactionManager.doInTransaction {
        val candidate = closedPlaceCandidateRepository.findByIdOrNull(candidateId) ?: return@doInTransaction null
        val place = placeRepository.findByIdOrNull(candidate.placeId) ?: return@doInTransaction null

        return@doInTransaction NamedClosedPlaceCandidate(
            candidateId = candidate.id,
            placeId = place.id,
            name = place.name,
            address = place.address.toString(),
            acceptedAt = candidate.acceptedAt,
            ignoredAt = candidate.ignoredAt,
        )
    }
}
