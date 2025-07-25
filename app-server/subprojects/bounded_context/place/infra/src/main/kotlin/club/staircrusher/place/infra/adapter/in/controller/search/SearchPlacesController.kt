package club.staircrusher.place.infra.adapter.`in`.controller.search

import club.staircrusher.api.converter.toModel
import club.staircrusher.api.spec.dto.GetAccessibilityPostRequest
import club.staircrusher.api.spec.dto.ListPlacesInBuildingPostRequest
import club.staircrusher.api.spec.dto.ListSearchKeywordsOfPlaceCategoryPost200Response
import club.staircrusher.api.spec.dto.ListSearchPlacePresetsResponseDto
import club.staircrusher.api.spec.dto.PlaceListItem
import club.staircrusher.api.spec.dto.SearchKeywordOfPlaceCategoryDto
import club.staircrusher.api.spec.dto.SearchPlacesPost200Response
import club.staircrusher.api.spec.dto.SearchPlacesPostRequest
import club.staircrusher.place.application.port.`in`.search.ListSearchKeywordOfPlaceCategoryUseCase
import club.staircrusher.place.application.port.`in`.search.PlaceSearchService
import club.staircrusher.place.application.port.`in`.search.SearchPlacePresetService
import club.staircrusher.spring_web.security.app.SccAppAuthentication
import club.staircrusher.stdlib.geography.Length
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class SearchPlacesController(
    private val placeSearchService: PlaceSearchService,
    private val searchPlacePresetService: SearchPlacePresetService,
    private val listSearchKeywordOfPlaceCategoryUseCase: ListSearchKeywordOfPlaceCategoryUseCase
) {
    @Suppress("MagicNumber")
    @PostMapping("/searchPlaces")
    suspend fun searchPlaces(
        @RequestBody request: SearchPlacesPostRequest,
        authentication: SccAppAuthentication?,
    ): SearchPlacesPost200Response {
        val userId = authentication?.principal
        val searchResults = placeSearchService.searchPlaces(
            searchText = request.searchText,
            currentLocation = request.currentLocation?.toModel(),
            distanceMetersLimit = Length.ofMeters(request.distanceMetersLimit.toDouble()),
            sort = request.sort?.value,
            filter = request.filters?.toModel(),
            limit = null,
            userId = userId
        )
        return SearchPlacesPost200Response(
            items = searchResults.map { it.toDTO() }
        )
    }

    @PostMapping("/listPlacesInBuilding")
    suspend fun listPlacesInBuilding(
        @RequestBody request: ListPlacesInBuildingPostRequest,
        authentication: SccAppAuthentication?,
    ): List<PlaceListItem> {
        val userId = authentication?.principal
        return placeSearchService.listPlacesInBuilding(
            buildingId = request.buildingId,
            userId = userId
        )
            .map { it.toDTO() }
    }

    @PostMapping("/getPlaceWithBuilding")
    suspend fun getPlace(
        @RequestBody request: GetAccessibilityPostRequest,
        authentication: SccAppAuthentication?,
    ): PlaceListItem {
        val userId = authentication?.principal
        return placeSearchService.getPlace(
            placeId = request.placeId,
            userId = userId
        ).toDTO()
    }

    @PostMapping("/listSearchKeywordsOfPlaceCategory")
    fun listSearchKeywordsOfPlaceCategory(): ListSearchKeywordsOfPlaceCategoryPost200Response {
        return ListSearchKeywordsOfPlaceCategoryPost200Response(
            items = listSearchKeywordOfPlaceCategoryUseCase.handle()
                .map { SearchKeywordOfPlaceCategoryDto(it.first.toDto(), it.second) }
        )
    }

    @PostMapping("/listSearchPlacePresets")
    fun listSearchPlacePresets(): ListSearchPlacePresetsResponseDto {
        val searchPresets = searchPlacePresetService.list()
        return ListSearchPlacePresetsResponseDto(
            keywordPresets = searchPresets.map { it.toDTO() }
        )
    }
}
