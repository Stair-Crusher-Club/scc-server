package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.persistence.AccessibilityAllowedRegionRepository
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.geography.LocationUtils

@Component
class AccessibilityAllowedRegionService(
    private val accessibilityAllowedRegionRepository: AccessibilityAllowedRegionRepository,
) {
    fun isAccessibilityAllowed(location: Location): Boolean {
        return accessibilityAllowedRegionRepository.findAll()
            .any {
                LocationUtils.isInPolygon(it.boundaryVertices, location)
            }
    }
}
