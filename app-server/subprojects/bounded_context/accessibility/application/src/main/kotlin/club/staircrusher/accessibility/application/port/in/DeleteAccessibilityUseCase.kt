package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.place.application.port.`in`.PlaceService
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class DeleteAccessibilityUseCase(
    private val transactionManager: TransactionManager,
    private val placeAccessibilityRepository: PlaceAccessibilityRepository,
    private val deleteAccessibilityAplService: DeleteAccessibilityAplService,
    private val buildingAccessibilityRepository: BuildingAccessibilityRepository,
    private val placeService: PlaceService,
) {
    fun handle(
        userId: String,
        placeAccessibilityId: String,
    ) : Unit = transactionManager.doInTransaction(TransactionIsolationLevel.SERIALIZABLE) {
        val placeAccessibility = placeAccessibilityRepository.findById(placeAccessibilityId)
        if (!placeAccessibility.isDeletable(userId)) {
            throw SccDomainException("삭제 가능한 장소 정보가 아닙니다.")
        }

        val place = placeService.findPlace(placeAccessibility.placeId)!!
        deleteAccessibilityAplService.deletePlaceAccessibility(placeAccessibility, place)

        val building = place.building
        if (placeAccessibilityRepository.findByBuildingId(building.id).isEmpty()) {
            val buildingAccessibility = buildingAccessibilityRepository.findByBuildingId(building.id) ?: return@doInTransaction
            deleteAccessibilityAplService.deleteBuildingAccessibility(buildingAccessibility, building)
        }
    }
}
