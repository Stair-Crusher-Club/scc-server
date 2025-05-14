package club.staircrusher.place.application.port.`in`.accessibility

import club.staircrusher.place.application.port.`in`.place.BuildingService
import club.staircrusher.place.application.port.out.accessibility.persistence.BuildingAccessibilityRepository
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager
import org.springframework.data.repository.findByIdOrNull

@Component
class DeleteBuildingAccessibilityUseCase(
    private val transactionManager: TransactionManager,
    private val deleteAccessibilityAplService: DeleteAccessibilityAplService,
    private val buildingAccessibilityRepository: BuildingAccessibilityRepository,
    private val buildingService: BuildingService,
) {
    fun handle(
        userId: String,
        buildingAccessibilityId: String,
    ) : Unit = transactionManager.doInTransaction(TransactionIsolationLevel.SERIALIZABLE) {
        val buildingAccessibility = buildingAccessibilityRepository.findByIdOrNull(buildingAccessibilityId)
            ?: throw SccDomainException("삭제 가능한 건물 정보가 아닙니다.")
        if (!buildingAccessibility.isDeletable(userId)) {
            throw SccDomainException("삭제 가능한 건물 정보가 아닙니다.")
        }

        val building = buildingService.getById(buildingAccessibility.buildingId)!!
        deleteAccessibilityAplService.deleteBuildingAccessibility(buildingAccessibility, building)
    }
}
