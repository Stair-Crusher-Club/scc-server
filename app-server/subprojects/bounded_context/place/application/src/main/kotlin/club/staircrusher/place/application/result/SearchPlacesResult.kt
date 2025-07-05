package club.staircrusher.place.application.result

import club.staircrusher.place.domain.model.accessibility.BuildingAccessibility
import club.staircrusher.place.domain.model.accessibility.PlaceAccessibility
import club.staircrusher.place.domain.model.place.Place
import club.staircrusher.stdlib.geography.Length

data class SearchPlacesResult(
    val place: Place,
    val buildingAccessibility: BuildingAccessibility?,
    val placeAccessibility: PlaceAccessibility?,
    val distance: Length? = null,
    val accessibilityScore: Double? = null,
    val isAccessibilityRegistrable: Boolean,
    val isFavoritePlace: Boolean,
    val placeReviewCount: Int?,
)
