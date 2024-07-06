package club.staircrusher.external_accessibility.application.port.`in`

import club.staircrusher.external_accessibility.application.port.out.persistence.ExternalAccessibilityRepository
import club.staircrusher.external_accessibility.domain.model.ExternalAccessibility
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.external_accessibility.ExternalAccessibilityCategory
import club.staircrusher.stdlib.geography.Length
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.util.string.isSimilarWith

@Component
class ExternalAccessibilityService(
    private val externalAccessibilityRepository: ExternalAccessibilityRepository,
) {
    fun get(
        id: String
    ): ExternalAccessibility {
        return externalAccessibilityRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("External Accessibility with id $id does not exist.")
    }
}
