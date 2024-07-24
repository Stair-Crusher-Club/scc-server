package club.staircrusher.place_search.infra.adapter.`in`.controller

import club.staircrusher.api.converter.toDTO
import club.staircrusher.api.spec.dto.EpochMillisTimestamp
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
    location = location.toDTO(),
)

fun PlaceSearchService.SearchPlacesResult.toDTO() = PlaceListItem(
    place = place.toDTO(),
    building = place.building.toDTO(),
    hasBuildingAccessibility = buildingAccessibility != null,
    hasPlaceAccessibility = placeAccessibility != null,
    distanceMeters = distance?.meter?.toInt(),
    isAccessibilityRegistrable = isAccessibilityRegistrable,
    accessibilityInfo = club.staircrusher.api.spec.dto.CompactAccessibilityInfoDto(
        accessibilityScore = accessibilityScore,
        floors = placeAccessibility?.floors ?: emptyList(),
        hasSlope = placeAccessibility?.hasSlope ?: false,
        imageUrls = placeAccessibility?.imageUrls ?: emptyList(),
        createdAt = placeAccessibility?.createdAt?.let { EpochMillisTimestamp(it.toEpochMilli()) },
    ),
)

@Suppress("ComplexMethod")
fun club.staircrusher.stdlib.place.PlaceCategory.toDto(): PlaceCategoryDto = when (this) {
    club.staircrusher.stdlib.place.PlaceCategory.ACCOMODATION -> PlaceCategoryDto.ACCOMODATION
    club.staircrusher.stdlib.place.PlaceCategory.CAFE -> PlaceCategoryDto.CAFE
    club.staircrusher.stdlib.place.PlaceCategory.RESTAURANT -> PlaceCategoryDto.RESTAURANT
    club.staircrusher.stdlib.place.PlaceCategory.MARKET -> PlaceCategoryDto.MARKET
    club.staircrusher.stdlib.place.PlaceCategory.CONVENIENCE_STORE -> PlaceCategoryDto.CONVENIENCE_STORE
    club.staircrusher.stdlib.place.PlaceCategory.KINDERGARTEN -> PlaceCategoryDto.KINDERGARTEN
    club.staircrusher.stdlib.place.PlaceCategory.SCHOOL -> PlaceCategoryDto.SCHOOL
    club.staircrusher.stdlib.place.PlaceCategory.ACADEMY -> PlaceCategoryDto.ACADEMY
    club.staircrusher.stdlib.place.PlaceCategory.PARKING_LOT -> PlaceCategoryDto.PARKING_LOT
    club.staircrusher.stdlib.place.PlaceCategory.GAS_STATION -> PlaceCategoryDto.GAS_STATION
    club.staircrusher.stdlib.place.PlaceCategory.SUBWAY_STATION -> PlaceCategoryDto.SUBWAY_STATION
    club.staircrusher.stdlib.place.PlaceCategory.BANK -> PlaceCategoryDto.BANK
    club.staircrusher.stdlib.place.PlaceCategory.CULTURAL_FACILITIES -> PlaceCategoryDto.CULTURAL_FACILITIES
    club.staircrusher.stdlib.place.PlaceCategory.AGENCY -> PlaceCategoryDto.AGENCY
    club.staircrusher.stdlib.place.PlaceCategory.PUBLIC_OFFICE -> PlaceCategoryDto.PUBLIC_OFFICE
    club.staircrusher.stdlib.place.PlaceCategory.ATTRACTION -> PlaceCategoryDto.ATTRACTION
    club.staircrusher.stdlib.place.PlaceCategory.HOSPITAL -> PlaceCategoryDto.HOSPITAL
    club.staircrusher.stdlib.place.PlaceCategory.PHARMACY -> PlaceCategoryDto.PHARMACY
}
