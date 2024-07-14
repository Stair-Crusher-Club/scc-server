package club.staircrusher.place.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.DeletePlaceFavoriteRequestDto
import club.staircrusher.api.spec.dto.DeletePlaceFavoriteResponseDto
import club.staircrusher.place.application.port.out.persistence.PlaceFavoriteRepository
import club.staircrusher.place.domain.model.PlaceFavorite
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.testing.spring_it.base.SccSpringITBase
import club.staircrusher.testing.spring_it.mock.MockSccClock
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration

class DeletePlaceFavoriteTest : SccSpringITBase() {

    @Autowired
    private lateinit var clock: MockSccClock

    @Autowired
    private lateinit var placeFavoriteRepository: PlaceFavoriteRepository

    @BeforeEach
    fun setUp() = transactionManager.doInTransaction {
        placeFavoriteRepository.removeAll()
    }

    @Test
    fun `즐겨찾기를 해제하면 해당 장소의 즐겨찾기 수가 감소한다`() {
        val (user, place) = transactionManager.doInTransaction {
            val user = testDataGenerator.createUser()
            val place = testDataGenerator.createBuildingAndPlace(placeName = "마루180")
            testDataGenerator.createPlaceFavorite(userId = user.id, placeId = place.id)
            user to place
        }
        clock.advanceTime(Duration.ofMinutes(10))
        val beforeRequestedAt = SccClock.instant()
        clock.advanceTime(Duration.ofSeconds(1))
        mvc
            .sccRequest("/deletePlaceFavorite", DeletePlaceFavoriteRequestDto(placeId = place.id), user = user)
            .apply {
                val result = getResult(DeletePlaceFavoriteResponseDto::class)
                assertTrue(result.totalPlaceFavoriteCount == 0L)
                val favorite = placeFavoriteRepository.findByUserIdAndPlaceId(user.id, place.id)
                assertTrue(favorite?.userId == user.id)
                assertTrue(favorite?.placeId == place.id)
                assertNotNull(favorite?.deletedAt)
                assertTrue(beforeRequestedAt.isBefore(favorite?.deletedAt))
            }
    }
}
