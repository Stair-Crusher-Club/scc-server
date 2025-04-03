package club.staircrusher.external_accessibility.infra.adapter.`in`.controller

import club.staircrusher.api.converter.toDTO
import club.staircrusher.api.spec.dto.ExternalAccessibility
import club.staircrusher.api.spec.dto.GetExternalAccessibilityPostRequest
import club.staircrusher.api.spec.dto.SearchExternalAccessibilitiesPost200Response
import club.staircrusher.api.spec.dto.SearchExternalAccessibilitiesPostRequest
import club.staircrusher.external_accessibility.application.port.out.persistence.ExternalAccessibilityRepository
import club.staircrusher.external_accessibility.infra.adapter.`in`.controller.base.ExternalAccessibilityITBase
import club.staircrusher.stdlib.geography.Length
import club.staircrusher.stdlib.geography.Location
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ExternalAccessibilityControllerTest : ExternalAccessibilityITBase() {

    @Autowired
    private lateinit var externalAccessibilityRepository: ExternalAccessibilityRepository

    @BeforeEach
    fun setUp() {
        transactionManager.doInTransaction {
            externalAccessibilityRepository.deleteAll()
        }
    }

    @Test
    fun 서치_정상_동작() {
        val currentLocation = Location(127.5, 37.5)
        // 데이터가 없다면 보여주지 않는다.
        val response = mvc.sccAnonymousRequest(
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
        val response2 = mvc.sccAnonymousRequest(
            "/searchExternalAccessibilities",
            SearchExternalAccessibilitiesPostRequest(
                currentLocation = currentLocation.toDTO(),
                distanceMetersLimit = 2000,
            )
        )
            .getResult(SearchExternalAccessibilitiesPost200Response::class).items ?: emptyList()
        assertTrue(response2.size == 2)

        // 범위 안의 데이터만 보여준다.
        val response3 = mvc.sccAnonymousRequest(
            "/searchExternalAccessibilities",
            SearchExternalAccessibilitiesPostRequest(
                currentLocation = currentLocation.toDTO(),
                distanceMetersLimit = 500,
            )
        )
            .getResult(SearchExternalAccessibilitiesPost200Response::class).items ?: emptyList()
        assertEquals(1, response3.size)
    }

    @Test
    fun 서치_단건_조회() {
        val data = testDataGenerator.createExternalAccessibility()
        val response = mvc.sccAnonymousRequest(
            "/getExternalAccessibility",
            GetExternalAccessibilityPostRequest(externalAccessibilityId = data.id)
        )
            .getResult(ExternalAccessibility::class)
        assertEquals(data.name, response.name)
    }
}
