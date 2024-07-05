package club.staircrusher.external_accessibility.application.port.out.persistence

import club.staircrusher.external_accessibility.domain.model.ExternalAccessibility
import club.staircrusher.stdlib.domain.repository.EntityRepository

@Suppress("TooManyFunctions")
interface ExternalAccessibilityRepository : EntityRepository<ExternalAccessibility, String> {
    fun findAll(): List<ExternalAccessibility>
}
