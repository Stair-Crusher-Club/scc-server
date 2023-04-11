package club.staircrusher.place_search.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.PlaceListItem
import club.staircrusher.place_search.application.port.`in`.PlaceSearchService
import club.staircrusher.place_search.domain.model.Building
import club.staircrusher.place_search.domain.model.Place

fun Place.toDTO() = club.staircrusher.api.spec.dto.Place(
    id = id,
    name = name,
    address = address,
)

fun Building.toDTO() = club.staircrusher.api.spec.dto.Building(
    id = id,
    address = address,
)

fun PlaceSearchService.SearchPlacesResult.toDTO() = PlaceListItem(
    place = place.toDTO(),
    building = place.building.toDTO(),
    hasBuildingAccessibility = buildingAccessibility != null,
    hasPlaceAccessibility = placeAccessibility != null,
    distanceMeters = distance?.meter?.toInt(),
    isAccessibilityRegistrable = isAccessibilityRegistrable,
)
