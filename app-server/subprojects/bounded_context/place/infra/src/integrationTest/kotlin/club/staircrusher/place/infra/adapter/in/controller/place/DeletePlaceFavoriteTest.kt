package club.staircrusher.place.infra.adapter.`in`.controller.place

import club.staircrusher.api.spec.dto.DeletePlaceFavoriteRequestDto
import club.staircrusher.api.spec.dto.DeletePlaceFavoriteResponseDto
import club.staircrusher.place.application.port.out.place.persistence.PlaceFavoriteRepository
import club.staircrusher.place.infra.adapter.`in`.controller.place.base.PlaceITBase
import club.staircrusher.stdlib.clock.SccClock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration

class DeletePlaceFavoriteTest : PlaceITBase() {

    @Autowired
    private lateinit var placeFavoriteRepository: PlaceFavoriteRepository

    @BeforeEach
    fun setUp() = transactionManager.doInTransaction {
        placeFavoriteRepository.deleteAll()
    }

    @Test
    fun `즐겨찾기를 해제하면 해당 장소의 즐겨찾기 수가 감소한다`() {
        val (user, place) = transactionManager.doInTransaction {
            val user = testDataGenerator.createIdentifiedUser().account
            val place = testDataGenerator.createBuildingAndPlace(placeName = "마루180")
            testDataGenerator.createPlaceFavorite(userId = user.id, placeId = place.id)
            user to place
        }
        clock.advanceTime(Duration.ofMinutes(10))
        val beforeRequestedAt = SccClock.instant()
        clock.advanceTime(Duration.ofSeconds(1))
        mvc
            .sccRequest("/deletePlaceFavorite", DeletePlaceFavoriteRequestDto(placeId = place.id), userAccount = user)
            .apply {
                val result = getResult(DeletePlaceFavoriteResponseDto::class)
                assertEquals(0, result.totalPlaceFavoriteCount)
                val favorite = placeFavoriteRepository.findFirstByUserIdAndPlaceId(user.id, place.id)
                assertEquals(user.id, favorite?.userId)
                assertEquals(place.id, favorite?.placeId)
                assertNotNull(favorite?.deletedAt)
                assertTrue(beforeRequestedAt.isBefore(favorite?.deletedAt))
            }
    }
}
