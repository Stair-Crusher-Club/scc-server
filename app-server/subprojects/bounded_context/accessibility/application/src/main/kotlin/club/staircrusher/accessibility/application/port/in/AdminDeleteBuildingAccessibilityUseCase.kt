package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityRepository
import club.staircrusher.place.application.port.`in`.BuildingService
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
        val buildingAccessibility = buildingAccessibilityRepository.findById(buildingAccessibilityId)
        val building = buildingService.getById(buildingAccessibility.buildingId)!!
        deleteAccessibilityAplService.deleteBuildingAccessibility(buildingAccessibility, building)
    }
}
