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

fun PlaceCategory.Companion.from(dto: club.staircrusher.api.spec.dto.PlaceCategory) =
    when (dto) {
        club.staircrusher.api.spec.dto.PlaceCategory.aCCOMODATION -> PlaceCategory.ACCOMODATION
        club.staircrusher.api.spec.dto.PlaceCategory.cAFE -> PlaceCategory.CAFE
        club.staircrusher.api.spec.dto.PlaceCategory.rESTAURANT -> PlaceCategory.RESTAURANT
        else -> null
    }

fun PlaceCategory.toDTO(): PlaceCategoryDto? = when (this) {
    PlaceCategory.ACCOMODATION -> PlaceCategoryDto(
        type = club.staircrusher.api.spec.dto.PlaceCategory.aCCOMODATION,
        this.humanReadableName
    )

    PlaceCategory.CAFE -> PlaceCategoryDto(
        type = club.staircrusher.api.spec.dto.PlaceCategory.cAFE,
        this.humanReadableName
    )

    PlaceCategory.RESTAURANT -> PlaceCategoryDto(
        type = club.staircrusher.api.spec.dto.PlaceCategory.rESTAURANT,
        this.humanReadableName
    )

    else -> null
}
