package club.staircrusher.place_search.infra.adapter.`in`.controller

import club.staircrusher.api.converter.toModel
import club.staircrusher.api.spec.dto.PlaceListItem
import club.staircrusher.api.spec.dto.SearchPlacesPost200Response
import club.staircrusher.api.spec.dto.SearchPlacesPostRequest
import club.staircrusher.place_search.application.service.PlaceSearchService
import club.staircrusher.place_search.infra.adapter.`in`.converter.toDTO
import club.staircrusher.stdlib.geography.Length
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class SearchPlacesController(
    private val placeSearchService: PlaceSearchService,
) {
    @PostMapping("/searchPlaces")
    suspend fun searchPlaces(@RequestBody request: SearchPlacesPostRequest): SearchPlacesPost200Response {
        val searchResults = placeSearchService.searchPlaces(
            searchText = request.searchText,
            currentLocation = request.currentLocation?.toModel(),
            distanceMetersLimit = Length.ofMeters(request.distanceMetersLimit.toDouble()),
            siGunGuId = request.siGunGuId,
            eupMyeonDongId = request.eupMyeonDongId,
        )
        return SearchPlacesPost200Response(
            items = searchResults.map {
                PlaceListItem(
                    place = it.place.toDTO(),
                    building = it.place.building.toDTO(),
                    hasBuildingAccessibility = it.hasBuildingAccessibility,
                    hasPlaceAccessibility = it.hasPlaceAccessibility,
                    distanceMeters = it.distanceMeters?.meter?.toInt(),
                )
            }
        )
    }
}
