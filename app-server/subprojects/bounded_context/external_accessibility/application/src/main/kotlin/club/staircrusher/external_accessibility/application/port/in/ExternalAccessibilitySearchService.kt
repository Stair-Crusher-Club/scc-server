package club.staircrusher.external_accessibility.application.port.`in`

import club.staircrusher.external_accessibility.application.port.out.persistence.ExternalAccessibilityRepository
import club.staircrusher.external_accessibility.domain.model.ExternalAccessibility
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.external_accessibility.ExternalAccessibilityCategory
import club.staircrusher.stdlib.geography.Length
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.geography.LocationUtils
import club.staircrusher.stdlib.util.string.isSimilarWith

@Component
class ExternalAccessibilitySearchService(
    private val externalAccessibilityRepository: ExternalAccessibilityRepository,
) {
    fun searchExternalAccessibilities(
        searchText: String?,
        currentLocation: Location?,
        distanceMetersLimit: Length,
        categories: List<ExternalAccessibilityCategory> = emptyList(),
    ): List<ExternalAccessibility> {
        return (
            if (categories.isEmpty()) externalAccessibilityRepository.findAll()
            else externalAccessibilityRepository.findByCategoryIn(categories.toSet())
            )
            // FIXME: 현재 fullscan 후 필터링 중, 나중에 spatial index 걸어주기
            .filter {
                currentLocation ?: return@filter true
                val distance = LocationUtils.calculateDistance(currentLocation, it.location)
                distance <= distanceMetersLimit
            }
            .filter {
                searchText ?: return@filter true
                it.name.isSimilarWith(pattern = searchText)
            }
    }
}
