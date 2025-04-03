package club.staircrusher.place.application.port.`in`.accessibility

import club.staircrusher.place.application.port.`in`.place.PlaceApplicationService
import club.staircrusher.place.application.port.out.accessibility.persistence.PlaceAccessibilityRepository
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class AdminDeletePlaceAccessibilityUseCase(
    private val transactionManager: TransactionManager,
    private val placeAccessibilityRepository: PlaceAccessibilityRepository,
    private val placeApplicationService: PlaceApplicationService,
    private val deleteAccessibilityAplService: DeleteAccessibilityAplService,
) {
    fun handle(
        placeAccessibilityId: String,
    ) : Unit = transactionManager.doInTransaction(TransactionIsolationLevel.SERIALIZABLE) {
        val placeAccessibility = placeAccessibilityRepository.findById(placeAccessibilityId).get()
        val place = placeApplicationService.findPlace(placeAccessibility.placeId)!!
        deleteAccessibilityAplService.deletePlaceAccessibility(placeAccessibility, place)
    }
}
