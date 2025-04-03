package club.staircrusher.place.application.port.out.accessibility.persistence

import club.staircrusher.place.domain.model.accessibility.AccessibilityAllowedRegion
import org.springframework.data.repository.CrudRepository

interface AccessibilityAllowedRegionRepository : CrudRepository<AccessibilityAllowedRegion, String>
