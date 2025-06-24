package club.staircrusher.place.domain.model.search

data class SearchPlaceFilter(
    val maxAccessibilityScore: Double?,
    val hasSlope: Boolean?,
    val isAccessibilityRegistered: Boolean?,
)
