package club.staircrusher.place_search.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.PlaceCategoryDto
import club.staircrusher.api.spec.dto.PlaceListItem
import club.staircrusher.place.domain.model.Building
import club.staircrusher.place.domain.model.Place
import club.staircrusher.place_search.application.port.`in`.PlaceSearchService
import club.staircrusher.stdlib.place.PlaceCategory

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

fun PlaceCategory.toDTO() = PlaceCategoryDto(
    type = when (this) {
        PlaceCategory.RESTAURANT -> club.staircrusher.api.spec.dto.PlaceCategory.rESTAURANT
        PlaceCategory.CAFE -> club.staircrusher.api.spec.dto.PlaceCategory.cAFE
        PlaceCategory.ACCOMODATION -> club.staircrusher.api.spec.dto.PlaceCategory.aCCOMODATION
        else -> club.staircrusher.api.spec.dto.PlaceCategory.eTC
    },
    name = this.humanReadableName
)
