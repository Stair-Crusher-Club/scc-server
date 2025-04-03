package club.staircrusher.place.infra.adapter.`in`.controller.place

import club.staircrusher.api.spec.dto.ListPlaceFavoritesByUserRequestDto
import club.staircrusher.api.spec.dto.ListPlaceFavoritesByUserResponseDto
import club.staircrusher.place.application.port.out.place.persistence.PlaceFavoriteRepository
import club.staircrusher.place.infra.adapter.`in`.controller.place.base.PlaceITBase
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ListPlaceFavoritesTest : PlaceITBase() {

    @Autowired
    private lateinit var placeFavoriteRepository: PlaceFavoriteRepository

    @BeforeEach
    fun setUp() = transactionManager.doInTransaction {
        placeFavoriteRepository.deleteAll()
    }

    @Test
    fun `사용자가 즐겨찾기 리스트와 즐겨찾기한 갯수를 내려준다`() {
        val (user, places) = transactionManager.doInTransaction {
            val user = testDataGenerator.createIdentifiedUser().account
            val places = listOf(
                testDataGenerator.createBuildingAndPlace(placeName = "마루180"),
                testDataGenerator.createBuildingAndPlace(placeName = "세인트존스호텔"),
                testDataGenerator.createBuildingAndPlace(placeName = "아크플레이스"),
                testDataGenerator.createBuildingAndPlace(placeName = "스타벅스"),
            )
            user to places
        }
        val favoritePlaces = places.subList(0, 2)
        transactionManager.doInTransaction {
            favoritePlaces.forEach { place ->
                testDataGenerator.createPlaceFavorite(
                    userId = user.id,
                    placeId = place.id
                )
            }
        }
        val notFavoritePlaces = places.subList(2, 4)
        mvc
            .sccRequest("/listPlaceFavoritesByUser", ListPlaceFavoritesByUserRequestDto(), userAccount = user)
            .apply {
                val result = getResult(ListPlaceFavoritesByUserResponseDto::class)
                Assertions.assertEquals(result.totalNumberOfItems, 2)
                Assertions.assertNotNull(result.items.firstOrNull { it.placeId == favoritePlaces[0].id })
                Assertions.assertNotNull(result.items.firstOrNull { it.placeId == favoritePlaces[1].id })
                Assertions.assertNull(result.items.firstOrNull { it.placeId == notFavoritePlaces[0].id })
                Assertions.assertNull(result.items.firstOrNull { it.placeId == notFavoritePlaces[1].id })
            }
    }
}
