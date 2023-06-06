package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.persistence.AccessibilityAllowedRegionRepository
import club.staircrusher.accessibility.domain.model.AccessibilityAllowedRegion
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class CreateAccessibilityAllowedRegionUseCase(
    private val transactionManager: TransactionManager,
    private val accessibilityAllowedRegionRepository: AccessibilityAllowedRegionRepository,
) {
    fun handle(
        name: String,
        boundaryVertices: List<Location>,
    ): AccessibilityAllowedRegion = transactionManager.doInTransaction {
        accessibilityAllowedRegionRepository.save(AccessibilityAllowedRegion(
            name = name,
            boundaryVertices = boundaryVertices,
        ))
    }
}
