package club.staircrusher.external_accessibility.application.port.out.persistence

import club.staircrusher.external_accessibility.domain.model.ExternalAccessibility
import club.staircrusher.stdlib.external_accessibility.ExternalAccessibilityCategory
import org.springframework.data.repository.CrudRepository

@Suppress("TooManyFunctions")
interface ExternalAccessibilityRepository : CrudRepository<ExternalAccessibility, String> {
    fun findByCategoryIn(categories: Set<ExternalAccessibilityCategory>): List<ExternalAccessibility>
}
