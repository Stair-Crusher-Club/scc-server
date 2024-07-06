package club.staircrusher.place.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.CreatePlaceFavoriteRequestDto
import club.staircrusher.api.spec.dto.CreatePlaceFavoriteResponseDto
import club.staircrusher.place.application.port.out.persistence.PlaceFavoriteRepository
import club.staircrusher.testing.spring_it.base.SccSpringITBase
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class CreatePlaceFavoriteTest : SccSpringITBase() {

    @Autowired
    private lateinit var placeFavoriteRepository: PlaceFavoriteRepository

    @BeforeEach
    fun setUp() = transactionManager.doInTransaction {
        placeFavoriteRepository.removeAll()
    }

    @Test
    fun `즐겨찾기하면 해당 장소의 즐겨찾기 수가 증가한다`() {
        val (user, place) = transactionManager.doInTransaction {
            val user = testDataGenerator.createUser()
            val place = testDataGenerator.createBuildingAndPlace(placeName = "마루180")
            user to place
        }
        mvc
            .sccRequest("/createPlaceFavorite", CreatePlaceFavoriteRequestDto(placeId = place.id), user = user)
            .apply {
                val result = getResult(CreatePlaceFavoriteResponseDto::class)
                assertTrue(result.placeFavorite.placeId == place.id)
                assertTrue(result.placeFavorite.userId == user.id)
                assertTrue(result.totalPlaceFavoriteCount == 1L)
            }
    }
}
