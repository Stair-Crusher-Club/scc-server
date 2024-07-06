package club.staircrusher.challenge.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.SearchExternalAccessibilitiesPostRequest
import club.staircrusher.api.spec.dto.SearchExternalAccessibilitiesPost200Response
import club.staircrusher.user.domain.model.User
import club.staircrusher.external_accessibility.infra.adapter.`in`.controller.base.ExternalAccessibilityITBase
import club.staircrusher.external_accessibility.infra.adapter.out.persistence.ExternalAccessibilityRepository
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
    fun `정상동작`() {
        val response = mvc.sccRequest(
            "/searchExternalAccessibilities",
            SearchExternalAccessibilitiesPostRequest(
                distanceMetersLimit = 2000,
            )
        )
            .getResult(SearchExternalAccessibilitiesPost200Response::class).items ?: emptyList()
        assertTrue(response.isEmpty())
    }
}
