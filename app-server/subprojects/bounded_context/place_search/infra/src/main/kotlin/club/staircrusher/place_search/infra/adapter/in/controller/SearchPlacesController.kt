package club.staircrusher.place_search.infra.adapter.`in`.controller

import club.staircrusher.api.converter.toModel
import club.staircrusher.api.spec.dto.ListPlacesInBuildingPostRequest
import club.staircrusher.api.spec.dto.ListSearchKeywordsOfPlaceCategoryPost200Response
import club.staircrusher.api.spec.dto.PlaceListItem
import club.staircrusher.api.spec.dto.SearchKeywordOfPlaceCategoryDto
import club.staircrusher.api.spec.dto.SearchPlacesPost200Response
import club.staircrusher.api.spec.dto.SearchPlacesPostRequest
import club.staircrusher.place_search.application.port.`in`.ListSearchKeywordOfPlaceCategoryUseCase
import club.staircrusher.place_search.application.port.`in`.PlaceSearchService
import club.staircrusher.spring_web.env.SccEnv
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
            siGunGuId = request.siGunGuId,
            eupMyeonDongId = request.eupMyeonDongId,
            sort = request.sort?.value,
            limit = if (SccEnv.isDev()) null else 50 // FIXME: 장소 개수가 너무 많으면 성능에 이슈가 있어서, 일단 최대 10개만 내려주게끔 수정한다.
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

    @PostMapping("/listSearchKeywordsOfPlaceCategory")
    fun listSearchKeywordsOfPlaceCategory(): ListSearchKeywordsOfPlaceCategoryPost200Response {
        return ListSearchKeywordsOfPlaceCategoryPost200Response(
            items = listSearchKeywordOfPlaceCategoryUseCase.handle()
                .map { SearchKeywordOfPlaceCategoryDto(it.first.toDto(), it.second) }
        )
    }
}
