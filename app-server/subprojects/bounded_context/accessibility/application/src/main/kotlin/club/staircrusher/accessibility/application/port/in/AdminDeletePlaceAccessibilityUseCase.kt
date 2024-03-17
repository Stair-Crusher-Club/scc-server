package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.place.application.port.`in`.PlaceService
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class AdminDeletePlaceAccessibilityUseCase(
    private val transactionManager: TransactionManager,
    private val placeAccessibilityRepository: PlaceAccessibilityRepository,
    private val placeService: PlaceService,
    private val deleteAccessibilityAplService: DeleteAccessibilityAplService,
) {
    fun handle(
        placeAccessibilityId: String,
    ) : Unit = transactionManager.doInTransaction(TransactionIsolationLevel.SERIALIZABLE) {
        val placeAccessibility = placeAccessibilityRepository.findById(placeAccessibilityId)
        val place = placeService.findPlace(placeAccessibility.placeId)!!
        deleteAccessibilityAplService.deletePlaceAccessibility(placeAccessibility, place)
    }
}
