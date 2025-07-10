package club.staircrusher.place.infra.adapter.`in`.controller.accessibility

import club.staircrusher.admin_api.spec.dto.AdminSearchAccessibilitiesResultDTO
import club.staircrusher.place.application.port.out.accessibility.persistence.BuildingAccessibilityRepository
import club.staircrusher.place.application.port.out.accessibility.persistence.PlaceAccessibilityRepository
import club.staircrusher.place.infra.adapter.`in`.controller.accessibility.base.AccessibilityITBase
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import java.time.Duration

class AdminAccessibilityTest : AccessibilityITBase() {

    @Autowired
    lateinit var placeAccessibilityRepository: PlaceAccessibilityRepository

    @Autowired
    lateinit var buildingAccessibilityRepository: BuildingAccessibilityRepository

    @BeforeEach
    fun setUp() {
        placeAccessibilityRepository.deleteAll()
        buildingAccessibilityRepository.deleteAll()
    }

    @Test
    fun `검색 결과에 이미지가 잘 포함된다`() {
        registerAccessibility(imageUrls = listOf("example.com/image1.jpg"))

        // 바로 검색하면 accessibility 의 createdAt 과 cursor 의 timestamp 가 동일해서 결과가 검색되지 않는다
        clock.advanceTime(Duration.ofMinutes(1L))
        mvc
            .sccAdminRequest("/admin/accessibilities/search", HttpMethod.GET, null)
            .andExpect {
                status { isOk() }
            }
            .apply {
                val result = getResult(AdminSearchAccessibilitiesResultDTO::class)
                Assertions.assertTrue(result.items.isNotEmpty())
                Assertions.assertTrue(result.items[0].placeAccessibility.images.isNotEmpty())
            }
    }
}
