package club.staircrusher.accessibility.domain.model

import java.time.Instant

data class PlaceAccessibilityComment(
    val id: String,
    val placeId: String,
    val userId: String?,
    val comment: String,
    val createdAt: Instant,
)
