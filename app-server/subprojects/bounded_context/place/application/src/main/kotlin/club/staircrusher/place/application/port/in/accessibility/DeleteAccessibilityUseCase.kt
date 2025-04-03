package club.staircrusher.place.application.port.`in`.accessibility

import club.staircrusher.place.application.port.`in`.place.PlaceApplicationService
import club.staircrusher.place.application.port.out.accessibility.persistence.BuildingAccessibilityRepository
import club.staircrusher.place.application.port.out.accessibility.persistence.PlaceAccessibilityRepository
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
    private val placeApplicationService: PlaceApplicationService,
) {
    fun handle(
        userId: String,
        placeAccessibilityId: String,
    ) : Unit = transactionManager.doInTransaction(TransactionIsolationLevel.SERIALIZABLE) {
        val placeAccessibility = placeAccessibilityRepository.findById(placeAccessibilityId).get()
        if (!placeAccessibility.isDeletable(userId)) {
            throw SccDomainException("삭제 가능한 장소 정보가 아닙니다.")
        }

        val place = placeApplicationService.findPlace(placeAccessibility.placeId)!!
        deleteAccessibilityAplService.deletePlaceAccessibility(placeAccessibility, place)

        val building = place.building
        val placeIds = placeApplicationService.findByBuildingId(building.id)
            .map { it.id }
            .toSet()
        if (placeAccessibilityRepository.findByPlaceIdInAndDeletedAtIsNull(placeIds).isEmpty()) {
            val buildingAccessibility = buildingAccessibilityRepository.findFirstByBuildingIdAndDeletedAtIsNull(building.id) ?: return@doInTransaction
            deleteAccessibilityAplService.deleteBuildingAccessibility(buildingAccessibility, building)
        }
    }
}
