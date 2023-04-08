package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityCommentRepository
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityCommentRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.accessibility.application.port.out.web.PlaceService
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class DeleteAccessibilityUseCase(
    private val transactionManager: TransactionManager,
    private val placeAccessibilityRepository: PlaceAccessibilityRepository,
    private val placeAccessibilityCommentRepository: PlaceAccessibilityCommentRepository,
    private val buildingAccessibilityRepository: BuildingAccessibilityRepository,
    private val buildingAccessibilityCommentRepository: BuildingAccessibilityCommentRepository,
    private val placeService: PlaceService,
) {
    fun handle(
        userId: String,
        placeAccessibilityId: String,
    ) : Unit = transactionManager.doInTransaction(TransactionIsolationLevel.SERIALIZABLE) {
        val placeAccessibility = placeAccessibilityRepository.findById(placeAccessibilityId)
        if (placeAccessibility.userId != userId) {
            throw SccDomainException("직접 등록한 장소 정보가 아닙니다.")
        }
        placeAccessibilityRepository.remove(placeAccessibilityId)
        placeAccessibilityCommentRepository.removeByPlaceId(placeAccessibility.placeId)

        val buildingId = placeService.findPlace(placeAccessibility.placeId)!!.buildingId
        if (placeAccessibilityRepository.findByBuildingId(buildingId).isEmpty()) {
            val buildingAccessibility = buildingAccessibilityRepository.findByBuildingId(buildingId) ?: return@doInTransaction
            buildingAccessibilityRepository.remove(buildingAccessibility.id)
            buildingAccessibilityCommentRepository.removeByBuildingId(buildingAccessibility.buildingId)
        }
    }
}
