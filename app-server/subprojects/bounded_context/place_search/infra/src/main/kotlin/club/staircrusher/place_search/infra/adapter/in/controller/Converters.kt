package club.staircrusher.place_search.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.PlaceCategoryDto
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

fun club.staircrusher.stdlib.place.PlaceCategory.toDto(): PlaceCategoryDto? = when (this) {
    club.staircrusher.stdlib.place.PlaceCategory.ACCOMODATION -> PlaceCategoryDto.aCCOMODATION
    club.staircrusher.stdlib.place.PlaceCategory.CAFE -> PlaceCategoryDto.cAFE
    club.staircrusher.stdlib.place.PlaceCategory.RESTAURANT -> PlaceCategoryDto.rESTAURANT
    club.staircrusher.stdlib.place.PlaceCategory.MARKET,
    club.staircrusher.stdlib.place.PlaceCategory.CONVENIENCE_STORE,
    club.staircrusher.stdlib.place.PlaceCategory.KINDERGARTEN,
    club.staircrusher.stdlib.place.PlaceCategory.SCHOOL,
    club.staircrusher.stdlib.place.PlaceCategory.ACADEMY,
    club.staircrusher.stdlib.place.PlaceCategory.PARKING_LOT,
    club.staircrusher.stdlib.place.PlaceCategory.GAS_STATION,
    club.staircrusher.stdlib.place.PlaceCategory.SUBWAY_STATION,
    club.staircrusher.stdlib.place.PlaceCategory.BANK,
    club.staircrusher.stdlib.place.PlaceCategory.CULTURAL_FACILITIES,
    club.staircrusher.stdlib.place.PlaceCategory.AGENCY,
    club.staircrusher.stdlib.place.PlaceCategory.PUBLIC_OFFICE,
    club.staircrusher.stdlib.place.PlaceCategory.ATTRACTION,
    club.staircrusher.stdlib.place.PlaceCategory.HOSPITAL,
    club.staircrusher.stdlib.place.PlaceCategory.PHARMACY -> null
}
