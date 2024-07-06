package club.staircrusher.external_accessibility.infra.adapter.`in`.controller

import club.staircrusher.api.converter.toDTO
import club.staircrusher.api.spec.dto.SearchExternalAccessibilitiesPostRequest
import club.staircrusher.api.spec.dto.SearchExternalAccessibilitiesPost200Response
import club.staircrusher.user.domain.model.User
import club.staircrusher.external_accessibility.infra.adapter.`in`.controller.base.ExternalAccessibilityITBase
import club.staircrusher.external_accessibility.infra.adapter.out.persistence.ExternalAccessibilityRepository
import club.staircrusher.stdlib.geography.Length
import club.staircrusher.stdlib.geography.Location
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.random.Random

class SearchExternalAccessibilitiesControllerTest : ExternalAccessibilityITBase() {

    @Autowired
    private lateinit var externalAccessibilityRepository: ExternalAccessibilityRepository

    @BeforeEach
    fun setUp() {
        transactionManager.doInTransaction {
            externalAccessibilityRepository.removeAll()
        }
    }

    @Test
    fun 정상_동작() {
        val currentLocation = Location(127.5, 37.5)
        // 데이터가 없다면 보여주지 않는다.
        val response = mvc.sccRequest(
            "/searchExternalAccessibilities",
            SearchExternalAccessibilitiesPostRequest(
                currentLocation = currentLocation.toDTO(),
                distanceMetersLimit = 2000,
            )
        )
            .getResult(SearchExternalAccessibilitiesPost200Response::class).items ?: emptyList()
        assertTrue(response.isEmpty())

        // 데이터가 존재한다면 보여준다.
        testDataGenerator.createExternalAccessibility(location = currentLocation)
        testDataGenerator.createExternalAccessibility(location = currentLocation.plusLat(Length.ofMeters(1000)))
        val response2 = mvc.sccRequest(
            "/searchExternalAccessibilities",
            SearchExternalAccessibilitiesPostRequest(
                currentLocation = currentLocation.toDTO(),
                distanceMetersLimit = 2000,
            )
        )
            .getResult(SearchExternalAccessibilitiesPost200Response::class).items ?: emptyList()
        assertTrue(response2.size == 2)

        // 범위 안의 데이터만 보여준다.
        val response3 = mvc.sccRequest(
            "/searchExternalAccessibilities",
            SearchExternalAccessibilitiesPostRequest(
                currentLocation = currentLocation.toDTO(),
                distanceMetersLimit = 500,
            )
        )
            .getResult(SearchExternalAccessibilitiesPost200Response::class).items ?: emptyList()
        assertEquals(1, response3.size)
    }
}
