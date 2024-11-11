package club.staircrusher.place.application.port.`in`

import club.staircrusher.place.application.port.out.persistence.ClosedPlaceCandidateRepository
import club.staircrusher.place.application.port.out.persistence.PlaceRepository
import club.staircrusher.place.application.result.NamedClosedPlaceCandidate
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager
import org.springframework.data.repository.findByIdOrNull

@Component
class AcceptClosedPlaceCandidateUseCase(
    private val transactionManager: TransactionManager,
    private val closedPlaceCandidateRepository: ClosedPlaceCandidateRepository,
    private val placeRepository: PlaceRepository,
) {
    fun handle(candidateId: String) = transactionManager.doInTransaction {
        val candidate = closedPlaceCandidateRepository.findByIdOrNull(candidateId)
            ?: throw IllegalArgumentException("closed place candidate with id($candidateId) not found")
        val place = placeRepository.findByIdOrNull(candidate.placeId)!!

        candidate.accept()
        closedPlaceCandidateRepository.save(candidate)
        place.setIsClosed(true)
        placeRepository.save(place)

        return@doInTransaction NamedClosedPlaceCandidate(
            candidateId = candidate.id,
            placeId = place.id,
            name = place.name,
            address = place.address.toString(),
            closedAt = candidate.closedAt,
            acceptedAt = candidate.acceptedAt,
            ignoredAt = candidate.ignoredAt,
        )
    }
}
