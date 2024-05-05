package club.staircrusher.place_search.infra.adapter.`in`.controller

import club.staircrusher.api.converter.toDTO
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
    accessibilityScore = accessibilityScore,
)

@Suppress("ComplexMethod")
fun club.staircrusher.stdlib.place.PlaceCategory.toDto(): PlaceCategoryDto = when (this) {
    club.staircrusher.stdlib.place.PlaceCategory.ACCOMODATION -> PlaceCategoryDto.aCCOMODATION
    club.staircrusher.stdlib.place.PlaceCategory.CAFE -> PlaceCategoryDto.cAFE
    club.staircrusher.stdlib.place.PlaceCategory.RESTAURANT -> PlaceCategoryDto.rESTAURANT
    club.staircrusher.stdlib.place.PlaceCategory.MARKET -> PlaceCategoryDto.mARKET
    club.staircrusher.stdlib.place.PlaceCategory.CONVENIENCE_STORE -> PlaceCategoryDto.cONVENIENCESTORE
    club.staircrusher.stdlib.place.PlaceCategory.KINDERGARTEN -> PlaceCategoryDto.kINDERGARTEN
    club.staircrusher.stdlib.place.PlaceCategory.SCHOOL -> PlaceCategoryDto.sCHOOL
    club.staircrusher.stdlib.place.PlaceCategory.ACADEMY -> PlaceCategoryDto.aCADEMY
    club.staircrusher.stdlib.place.PlaceCategory.PARKING_LOT -> PlaceCategoryDto.pARKINGLOT
    club.staircrusher.stdlib.place.PlaceCategory.GAS_STATION -> PlaceCategoryDto.gASSTATION
    club.staircrusher.stdlib.place.PlaceCategory.SUBWAY_STATION -> PlaceCategoryDto.sUBWAYSTATION
    club.staircrusher.stdlib.place.PlaceCategory.BANK -> PlaceCategoryDto.bANK
    club.staircrusher.stdlib.place.PlaceCategory.CULTURAL_FACILITIES -> PlaceCategoryDto.cULTURALFACILITIES
    club.staircrusher.stdlib.place.PlaceCategory.AGENCY -> PlaceCategoryDto.aGENCY
    club.staircrusher.stdlib.place.PlaceCategory.PUBLIC_OFFICE -> PlaceCategoryDto.pUBLICOFFICE
    club.staircrusher.stdlib.place.PlaceCategory.ATTRACTION -> PlaceCategoryDto.aTTRACTION
    club.staircrusher.stdlib.place.PlaceCategory.HOSPITAL -> PlaceCategoryDto.hOSPITAL
    club.staircrusher.stdlib.place.PlaceCategory.PHARMACY -> PlaceCategoryDto.pHARMACY
}
