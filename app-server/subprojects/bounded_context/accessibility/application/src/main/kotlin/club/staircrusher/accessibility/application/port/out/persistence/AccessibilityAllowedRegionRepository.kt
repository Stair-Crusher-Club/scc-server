package club.staircrusher.accessibility.application.port.out.persistence

import club.staircrusher.accessibility.domain.model.AccessibilityAllowedRegion
import org.springframework.data.repository.CrudRepository

interface AccessibilityAllowedRegionRepository : CrudRepository<AccessibilityAllowedRegion, String>
