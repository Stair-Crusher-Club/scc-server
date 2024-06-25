package club.staircrusher.place_search.infra.adapter.`in`.controller

import club.staircrusher.api.converter.toModel
import club.staircrusher.api.spec.dto.GetAccessibilityPostRequest
import club.staircrusher.api.spec.dto.ListPlacesInBuildingPostRequest
import club.staircrusher.api.spec.dto.ListSearchKeywordsOfPlaceCategoryPost200Response
import club.staircrusher.api.spec.dto.PlaceListItem
import club.staircrusher.api.spec.dto.SearchKeywordOfPlaceCategoryDto
import club.staircrusher.api.spec.dto.SearchPlacesPost200Response
import club.staircrusher.api.spec.dto.SearchPlacesPostRequest
import club.staircrusher.place_search.application.port.`in`.ListSearchKeywordOfPlaceCategoryUseCase
import club.staircrusher.place_search.application.port.`in`.PlaceSearchService
import club.staircrusher.stdlib.geography.Length
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class SearchPlacesController(
    private val placeSearchService: PlaceSearchService,
    private val listSearchKeywordOfPlaceCategoryUseCase: ListSearchKeywordOfPlaceCategoryUseCase
) {
    @Suppress("MagicNumber")
    @PostMapping("/searchPlaces")
    suspend fun searchPlaces(@RequestBody request: SearchPlacesPostRequest): SearchPlacesPost200Response {
        val searchResults = placeSearchService.searchPlaces(
            searchText = request.searchText,
            currentLocation = request.currentLocation?.toModel(),
            distanceMetersLimit = Length.ofMeters(request.distanceMetersLimit.toDouble()),
            sort = request.sort?.value,
            maxAccessibilityScore = request.filters?.maxAccessibilityScore,
            hasSlope = request.filters?.hasSlope,
            isAccessibilityRegistered = request.filters?.isRegistered,
            limit = null,
        )
        return SearchPlacesPost200Response(
            items = searchResults.map { it.toDTO() }
        )
    }

    @PostMapping("/listPlacesInBuilding")
    suspend fun listPlacesInBuilding(@RequestBody request: ListPlacesInBuildingPostRequest): List<PlaceListItem> {
        return placeSearchService.listPlacesInBuilding(request.buildingId)
            .map { it.toDTO() }
    }

    @PostMapping("/getPlaceWithBuilding")
    suspend fun getPlace(@RequestBody request: GetAccessibilityPostRequest): PlaceListItem {
        return placeSearchService.getPlace(request.placeId).toDTO()
    }

    @PostMapping("/listSearchKeywordsOfPlaceCategory")
    fun listSearchKeywordsOfPlaceCategory(): ListSearchKeywordsOfPlaceCategoryPost200Response {
        return ListSearchKeywordsOfPlaceCategoryPost200Response(
            items = listSearchKeywordOfPlaceCategoryUseCase.handle()
                .map { SearchKeywordOfPlaceCategoryDto(it.first.toDto(), it.second) }
        )
    }
}
