package club.staircrusher.place.infra.adapter.`in`.controller.search

import club.staircrusher.api.spec.dto.ListConqueredPlacesResponseDto
import club.staircrusher.place.infra.adapter.`in`.controller.search.base.PlaceSearchITBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration

class ListConqueredPlacesTest : PlaceSearchITBase() {

    @Test
    fun testListConqueredPlaces() {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createIdentifiedUser().account
        }
        val registeredCount = 10
        val places = transactionManager.doInTransaction {
            (1..registeredCount).map {
                val place = testDataGenerator.createBuildingAndPlace()
                testDataGenerator.registerBuildingAndPlaceAccessibility(place, user)
                place
            }
        }

        mvc
            .sccRequest("/listConqueredPlaces", requestBody = null, userAccount = user)
            .apply {
                val result = getResult(ListConqueredPlacesResponseDto::class)
                assertEquals(registeredCount.toLong(), result.totalNumberOfItems)
                result.items.forEach { item ->
                    val place = places.find { it.id == item.place.id }!!
                    assertEquals(place.building.id, item.building.id)
                    assertTrue(item.hasPlaceAccessibility)
                    assertTrue(item.hasBuildingAccessibility)
                    assertNull(item.distanceMeters)
                }
            }
    }

    @Test
    fun 생성_시간_역순으로_내려준다() {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createIdentifiedUser().account
        }

        val placeIdsOrderedByCreatedAtDesc = (1..10).map {
            clock.advanceTime(Duration.ofHours(1))
            transactionManager.doInTransaction {
                val place = testDataGenerator.createBuildingAndPlace()
                val (pa, _) = testDataGenerator.registerBuildingAndPlaceAccessibility(place, user)
                place to pa
            }
        }
            .sortedByDescending { it.second.createdAt }
            .map { it.first.id }

        // 곧바로 request 를 날리면 마지막에 등록한 pa 가 잡히지 않는다
        // cursor.initial 의 createdAt (즉, SccClock.instant()) 보다 작아야 한다는 조건 때문에 그런 것으로 추정
        clock.advanceTime(Duration.ofSeconds(1))

        mvc
            .sccRequest("/listConqueredPlaces", requestBody = null, userAccount = user)
            .apply {
                val result = getResult(ListConqueredPlacesResponseDto::class)
                assertEquals(
                    placeIdsOrderedByCreatedAtDesc,
                    result.items.map { it.place.id },
                )
            }
    }
}
