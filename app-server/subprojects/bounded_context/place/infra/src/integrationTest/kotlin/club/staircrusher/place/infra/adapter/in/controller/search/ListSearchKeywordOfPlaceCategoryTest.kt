package club.staircrusher.place.infra.adapter.`in`.controller.search

import club.staircrusher.api.spec.dto.ListSearchKeywordsOfPlaceCategoryPost200Response
import club.staircrusher.place.application.port.`in`.search.ListSearchKeywordOfPlaceCategoryUseCase
import club.staircrusher.place.infra.adapter.`in`.controller.search.base.PlaceSearchITBase
import com.fasterxml.jackson.core.type.TypeReference
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ListSearchKeywordOfPlaceCategoryTest : PlaceSearchITBase() {
    @Autowired
    lateinit var listSearchKeywordOfPlaceCategoryUseCase: ListSearchKeywordOfPlaceCategoryUseCase

    @Test
    fun testListPlacesInBuilding() {

        mvc
            .sccAnonymousRequest("/listSearchKeywordsOfPlaceCategory", null)
            .apply {
                val result = getResult(object : TypeReference<ListSearchKeywordsOfPlaceCategoryPost200Response>() {})
                assertEquals(result.items.size, listSearchKeywordOfPlaceCategoryUseCase.handle().size)
            }
    }
}
