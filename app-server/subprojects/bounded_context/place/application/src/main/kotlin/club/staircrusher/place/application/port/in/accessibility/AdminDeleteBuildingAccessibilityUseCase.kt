package club.staircrusher.place.application.port.`in`.accessibility

import club.staircrusher.place.application.port.`in`.place.BuildingService
import club.staircrusher.place.application.port.out.accessibility.persistence.BuildingAccessibilityRepository
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class AdminDeleteBuildingAccessibilityUseCase(
    private val transactionManager: TransactionManager,
    private val buildingAccessibilityRepository: BuildingAccessibilityRepository,
    private val buildingService: BuildingService,
    private val deleteAccessibilityAplService: DeleteAccessibilityAplService,
) {
    fun handle(
        buildingAccessibilityId: String,
    ) : Unit = transactionManager.doInTransaction(TransactionIsolationLevel.SERIALIZABLE) {
        val buildingAccessibility = buildingAccessibilityRepository.findById(buildingAccessibilityId).get()
        val building = buildingService.getById(buildingAccessibility.buildingId)!!
        deleteAccessibilityAplService.deleteBuildingAccessibility(buildingAccessibility, building)
    }
}
