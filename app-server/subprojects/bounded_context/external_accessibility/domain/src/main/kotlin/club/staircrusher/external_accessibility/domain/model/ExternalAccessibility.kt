package club.staircrusher.external_accessibility.domain.model

import club.staircrusher.stdlib.external_accessibility.ExternalAccessibilityCategory
import club.staircrusher.stdlib.geography.Location
import java.time.Instant

data class ExternalAccessibility(
    val id: String,
    val name: String,
    val location: Location,
    val address: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val category: ExternalAccessibilityCategory,
    val toiletDetails: ToiletAccessibilityDetails?,
)
