package club.staircrusher.place.application.port.`in`.accessibility

import club.staircrusher.place.application.port.out.accessibility.persistence.AccessibilityAllowedRegionRepository
import club.staircrusher.place.domain.model.accessibility.AccessibilityAllowedRegion
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
