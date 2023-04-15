package club.staircrusher.place_search.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.PlaceListItem
import club.staircrusher.place.domain.model.Building
import club.staircrusher.place.domain.model.Place
import club.staircrusher.place_search.application.port.`in`.PlaceSearchService

fun Place.toDTO() = club.staircrusher.api.spec.dto.Place(
    id = id,
    name = name,
    address = address.toString(),
)

fun Building.toDTO() = club.staircrusher.api.spec.dto.Building(
    id = id,
    address = address.toString(),
)

fun PlaceSearchService.SearchPlacesResult.toDTO() = PlaceListItem(
    place = place.toDTO(),
    building = place.building.toDTO(),
    hasBuildingAccessibility = buildingAccessibility != null,
    hasPlaceAccessibility = placeAccessibility != null,
    distanceMeters = distance?.meter?.toInt(),
    isAccessibilityRegistrable = isAccessibilityRegistrable,
)
