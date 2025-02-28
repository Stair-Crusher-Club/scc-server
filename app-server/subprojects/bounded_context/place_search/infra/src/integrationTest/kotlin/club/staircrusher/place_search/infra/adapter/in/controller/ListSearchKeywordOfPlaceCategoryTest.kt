package club.staircrusher.place_search.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.ListSearchKeywordsOfPlaceCategoryPost200Response
import club.staircrusher.place_search.application.port.`in`.ListSearchKeywordOfPlaceCategoryUseCase
import club.staircrusher.place_search.infra.adapter.`in`.controller.base.PlaceSearchITBase
import com.fasterxml.jackson.core.type.TypeReference
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ListSearchKeywordOfPlaceCategoryTest : PlaceSearchITBase() {
    @Test
    fun testListPlacesInBuilding() {

        mvc
            .sccAnonymousRequest("/listSearchKeywordsOfPlaceCategory", null)
            .apply {
                val result = getResult(object : TypeReference<ListSearchKeywordsOfPlaceCategoryPost200Response>() {})
                assertEquals(result.items.size, ListSearchKeywordOfPlaceCategoryUseCase().handle().size)
            }
    }
}
