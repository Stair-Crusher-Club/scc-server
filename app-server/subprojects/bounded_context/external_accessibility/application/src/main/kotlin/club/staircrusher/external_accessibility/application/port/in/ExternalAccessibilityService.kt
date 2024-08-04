package club.staircrusher.external_accessibility.application.port.`in`

import club.staircrusher.external_accessibility.application.port.out.persistence.ExternalAccessibilityRepository
import club.staircrusher.external_accessibility.domain.model.ExternalAccessibility
import club.staircrusher.stdlib.di.annotation.Component

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

    fun upsert(
        list: List<ExternalAccessibility>
    ) {
        return externalAccessibilityRepository.saveAll(list)
    }
}
