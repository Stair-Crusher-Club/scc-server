package club.staircrusher.external_accessibility.domain.model

import java.time.Instant

data class ExternalAccessibility(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)
