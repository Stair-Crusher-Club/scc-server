package club.staircrusher.place.application.port.`in`.accessibility

import club.staircrusher.place.application.port.`in`.place.PlaceApplicationService
import club.staircrusher.place.application.port.out.accessibility.persistence.PlaceAccessibilityRepository
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager
import org.springframework.data.repository.findByIdOrNull

@Component
class DeletePlaceAccessibilityUseCase(
    private val transactionManager: TransactionManager,
    private val placeAccessibilityRepository: PlaceAccessibilityRepository,
    private val deleteAccessibilityAplService: DeleteAccessibilityAplService,
    private val placeApplicationService: PlaceApplicationService,
) {
    fun handle(
        userId: String,
        placeAccessibilityId: String,
    ) : Unit = transactionManager.doInTransaction(TransactionIsolationLevel.SERIALIZABLE) {
        val placeAccessibility = placeAccessibilityRepository.findByIdOrNull(placeAccessibilityId)
            ?: throw SccDomainException("삭제 가능한 장소 정보가 아닙니다.")
        if (!placeAccessibility.isDeletable(userId)) {
            throw SccDomainException("삭제 가능한 장소 정보가 아닙니다.")
        }

        val place = placeApplicationService.findPlace(placeAccessibility.placeId)!!
        deleteAccessibilityAplService.deletePlaceAccessibility(placeAccessibility, place)
    }
}
