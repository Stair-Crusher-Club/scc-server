package club.staircrusher.accessibility.application.port.out.persistence

import club.staircrusher.accessibility.domain.model.AccessibilityAllowedRegion
import club.staircrusher.stdlib.domain.repository.EntityRepository

interface AccessibilityAllowedRegionRepository : EntityRepository<AccessibilityAllowedRegion, String> {
    fun findAll(): List<AccessibilityAllowedRegion>
}
