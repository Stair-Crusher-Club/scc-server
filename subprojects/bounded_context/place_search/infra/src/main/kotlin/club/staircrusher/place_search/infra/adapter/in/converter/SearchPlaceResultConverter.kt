package club.staircrusher.place_search.infra.adapter.`in`.converter

import club.staircrusher.api.spec.dto.PlaceListItem
import club.staircrusher.place_search.application.service.PlaceSearchService

fun PlaceSearchService.SearchPlacesResult.toDTO() = PlaceListItem(
    place = place.toDTO(),
    building = place.building.toDTO(),
    hasBuildingAccessibility = hasBuildingAccessibility,
    hasPlaceAccessibility = hasPlaceAccessibility,
    distanceMeters = distanceMeters?.meter?.toInt(),
)
