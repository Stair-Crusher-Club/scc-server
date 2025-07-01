package club.staircrusher.place.infra.adapter.`in`.controller.accessibility

import club.staircrusher.api.spec.dto.GetNearbyAccessibilityStatusPost200Response
import club.staircrusher.api.spec.dto.GetNearbyAccessibilityStatusPostRequest
import club.staircrusher.api.spec.dto.Location
import club.staircrusher.place.application.port.out.accessibility.persistence.PlaceAccessibilityRepository
import club.staircrusher.place.infra.adapter.`in`.controller.accessibility.base.AccessibilityITBase
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class CountNearbyAccessibilityTest : AccessibilityITBase() {
    @Autowired
    lateinit var placeAccessibilityRepository: PlaceAccessibilityRepository

    @BeforeEach
    fun setUp() = transactionManager.doInTransaction {
        placeAccessibilityRepository.deleteAll()
    }

    @Test
    fun `주변에 접근성 정보가 등록된 장소의 수를 내려준다`() {
        // Given
        val (user, place, _, _) = registerAccessibility()
        val params = GetNearbyAccessibilityStatusPostRequest(
            currentLocation = Location(
                lat= place.location.lat,
                lng = place.location.lng,
            ),
            distanceMetersLimit = 500,
        )

        // When
        mvc.sccRequest("/getNearbyAccessibilityStatus", params, user)
            .apply {
                val result = getResult(GetNearbyAccessibilityStatusPost200Response::class)
                Assertions.assertEquals(1, result.conqueredCount)
            }

        val paramsVeryFarCurrentLocation = GetNearbyAccessibilityStatusPostRequest(
            currentLocation = Location(
                lat = place.location.lat + 1,
                lng = place.location.lng + 1,
            ),
            distanceMetersLimit = 500,
        )
        mvc.sccRequest("/getNearbyAccessibilityStatus", paramsVeryFarCurrentLocation, user)
            .apply {
                val result = getResult(GetNearbyAccessibilityStatusPost200Response::class)
                Assertions.assertEquals(0, result.conqueredCount)
            }
    }
}
