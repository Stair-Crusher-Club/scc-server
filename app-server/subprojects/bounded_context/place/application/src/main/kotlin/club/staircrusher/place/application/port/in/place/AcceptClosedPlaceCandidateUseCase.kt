package club.staircrusher.place.application.port.`in`.place

import club.staircrusher.place.application.port.`in`.accessibility.DeleteAccessibilityAplService
import club.staircrusher.place.application.port.out.accessibility.persistence.PlaceAccessibilityRepository
import club.staircrusher.place.application.port.out.place.persistence.ClosedPlaceCandidateRepository
import club.staircrusher.place.application.port.out.place.persistence.PlaceRepository
import club.staircrusher.place.application.result.NamedClosedPlaceCandidate
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager
import org.springframework.data.repository.findByIdOrNull

@Component
class AcceptClosedPlaceCandidateUseCase(
    private val transactionManager: TransactionManager,
    private val closedPlaceCandidateRepository: ClosedPlaceCandidateRepository,
    private val placeRepository: PlaceRepository,
    private val placeAccessibilityRepository: PlaceAccessibilityRepository,
    private val deleteAccessibilityAplService: DeleteAccessibilityAplService,
) {
    fun handle(candidateId: String) = transactionManager.doInTransaction {
        val candidate = closedPlaceCandidateRepository.findByIdOrNull(candidateId)
            ?: throw IllegalArgumentException("closed place candidate with id($candidateId) not found")
        val place = placeRepository.findByIdOrNull(candidate.placeId)!!
        val placeAccessibility = placeAccessibilityRepository.findFirstByPlaceIdAndDeletedAtIsNull(place.id)

        candidate.accept()
        closedPlaceCandidateRepository.save(candidate)
        place.setIsClosed(true)
        placeRepository.save(place)
        placeAccessibility?.let { deleteAccessibilityAplService.deletePlaceAccessibility(it, place) }

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
