package club.staircrusher.accesssibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.application.port.`in`.image.ImageProcessor
import club.staircrusher.accessibility.application.port.out.DetectFacesResponse
import club.staircrusher.accessibility.application.port.out.DetectFacesService
import club.staircrusher.accessibility.application.port.out.persistence.AccessibilityImageFaceBlurringHistoryRepository
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.accessibility.domain.model.AccessibilityImage
import club.staircrusher.accessibility.domain.model.AccessibilityImageFaceBlurringHistory
import club.staircrusher.accesssibility.infra.adapter.`in`.controller.base.AccessibilityITBase
import club.staircrusher.stdlib.Rect
import club.staircrusher.stdlib.Size
import club.staircrusher.stdlib.testing.SccRandom
import club.staircrusher.testing.spring_it.ITDataGenerator
import club.staircrusher.testing.spring_it.mock.MockDetectFacesService
import club.staircrusher.testing.spring_it.mock.MockSccClock
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import java.time.Duration

class BlurFacesInLatestAccessibilityTest : AccessibilityITBase() {
    @Autowired
    private lateinit var clock: MockSccClock

    @Autowired
    private lateinit var dataGenerator: ITDataGenerator

    @Autowired
    private lateinit var accessibilityImageFaceBlurringHistoryRepository: AccessibilityImageFaceBlurringHistoryRepository

    @Autowired
    private lateinit var placeAccessibilityRepository: PlaceAccessibilityRepository

    @Autowired
    private lateinit var buildingAccessibilityRepository: BuildingAccessibilityRepository

    @MockBean
    private lateinit var imageProcessor: ImageProcessor

    @MockBean
    private lateinit var detectFacesService: DetectFacesService

    @BeforeEach
    fun setUp() = runBlocking {
        val imageBytes = ByteArray(10) { it.toByte() }
        Mockito.`when`(detectFacesService.detect(eq(MockDetectFacesService.URL_WITH_FACES))).thenReturn(
            DetectFacesResponse(
                imageBytes = imageBytes,
                imageSize = Size(100, 100),
                positions = listOf(Rect(0, 0, 10, 10))
            )
        )
        Mockito.`when`(detectFacesService.detect(eq(MockDetectFacesService.URL_WITHOUT_FACES))).thenReturn(
            DetectFacesResponse(
                imageBytes = imageBytes,
                imageSize = Size(100, 100),
                positions = emptyList()
            )
        )
        Mockito.`when`(imageProcessor.blur(any(), any())).thenReturn(imageBytes)

        placeAccessibilityRepository.removeAll()
        buildingAccessibilityRepository.removeAll()
        accessibilityImageFaceBlurringHistoryRepository.removeAll()
    }

    @Test
    fun `얼굴 블러링 기록이 없으면 가장 오래된 place accessibility 의 이미지부터 얼굴 블러링한다`() = runBlocking {
        val (_, oldestPlaceAccessibility, _) = registerPlaceAccessibilityAndBuildingAccessibility(
            listOf(MockDetectFacesService.URL_WITH_FACES)
        )
        clock.advanceTime(Duration.ofMinutes(1))
        val (_, secondOldestPlaceAccessibility, _) = registerPlaceAccessibilityAndBuildingAccessibility(
            listOf(MockDetectFacesService.URL_WITH_FACES)
        )

        mvc.sccRequest("/blurFacesInLatestPlaceAccessibilityImages", null)

        val blurredResult =
            accessibilityImageFaceBlurringHistoryRepository.findByPlaceAccessibilityId(oldestPlaceAccessibility.id)
        Assertions.assertTrue(blurredResult.isNotEmpty())
        val notBlurredResult =
            accessibilityImageFaceBlurringHistoryRepository.findByPlaceAccessibilityId(secondOldestPlaceAccessibility.id)
        Assertions.assertTrue(notBlurredResult.isNotEmpty())
    }

    @Test
    fun `얼굴 블러링 기록이 이후 가장 오래된 place accessibility 의 이미지부터 얼굴 블러링한다`() = runBlocking {
        val (_, oldestPlaceAccessibility, _) = registerPlaceAccessibilityAndBuildingAccessibility(
            listOf(MockDetectFacesService.URL_WITH_FACES)
        )
        clock.advanceTime(Duration.ofMinutes(1))
        val (_, secondOldestPlaceAccessibility, _) = registerPlaceAccessibilityAndBuildingAccessibility(
            listOf(MockDetectFacesService.URL_WITH_FACES)
        )
        transactionManager.doInTransaction {
            accessibilityImageFaceBlurringHistoryRepository.save(
                AccessibilityImageFaceBlurringHistory(
                    id = "", placeAccessibilityId = oldestPlaceAccessibility.id,
                    buildingAccessibilityId = null,
                    beforeImageUrl = "image_url", afterImageUrl = "blurred_image_url",
                    detectedPeopleCount = null,
                    createdAt = clock.instant(),
                    updatedAt = clock.instant()
                )
            )
        }

        mvc.sccRequest("/blurFacesInLatestPlaceAccessibilityImages", null)

        val result =
            accessibilityImageFaceBlurringHistoryRepository.findByPlaceAccessibilityId(secondOldestPlaceAccessibility.id)
        Assertions.assertTrue(result.isNotEmpty())
    }

    @Test
    fun `얼굴 블러링 기록이 없으면 가장 오래된 building accessibility 의 이미지부터 얼굴 블러링한다`() = runBlocking {
        val (_, _, oldestBuildingAccessibility) = registerPlaceAccessibilityAndBuildingAccessibility(
            listOf(MockDetectFacesService.URL_WITH_FACES)
        )
        clock.advanceTime(Duration.ofMinutes(1))
        val (_, _, secondOldestBuildingAccessibility) = registerPlaceAccessibilityAndBuildingAccessibility(
            listOf(MockDetectFacesService.URL_WITH_FACES)
        )

        mvc.sccRequest("/blurFacesInLatestPlaceAccessibilityImages", null)

        val blurredResult =
            accessibilityImageFaceBlurringHistoryRepository.findByBuildingAccessibilityId(oldestBuildingAccessibility.id)
        Assertions.assertTrue(blurredResult.isNotEmpty())
        val notBlurredResult =
            accessibilityImageFaceBlurringHistoryRepository.findByBuildingAccessibilityId(
                secondOldestBuildingAccessibility.id
            )
        Assertions.assertTrue(notBlurredResult.isNotEmpty())
    }

    @Test
    fun `얼굴 블러링 기록이 이후 가장 오래된 building accessibility 의 이미지부터 얼굴 블러링한다`() = runBlocking {
        val (_, _, oldestBuildingAccessibility) = registerPlaceAccessibilityAndBuildingAccessibility(
            listOf(MockDetectFacesService.URL_WITH_FACES)
        )
        clock.advanceTime(Duration.ofMinutes(1))
        val (_, _, secondOldestBuildingAccessibility) = registerPlaceAccessibilityAndBuildingAccessibility(
            listOf(MockDetectFacesService.URL_WITH_FACES)
        )
        transactionManager.doInTransaction {
            accessibilityImageFaceBlurringHistoryRepository.save(
                AccessibilityImageFaceBlurringHistory(
                    id = "", placeAccessibilityId = null,
                    buildingAccessibilityId = oldestBuildingAccessibility.id,
                    beforeImageUrl = "image_url", afterImageUrl = "blurred_image_url",
                    detectedPeopleCount = null,
                    createdAt = clock.instant(),
                    updatedAt = clock.instant()
                )
            )
        }

        mvc.sccRequest("/blurFacesInLatestPlaceAccessibilityImages", null)

        val result =
            accessibilityImageFaceBlurringHistoryRepository.findByBuildingAccessibilityId(
                secondOldestBuildingAccessibility.id
            )
        Assertions.assertTrue(result.isNotEmpty())
    }


    private fun registerPlaceAccessibilityAndBuildingAccessibility(imageUrls: List<String>) =
        transactionManager.doInTransaction {
            val user = dataGenerator.createUser()
            val building = dataGenerator.createBuilding()
            dataGenerator.registerBuildingAccessibilityIfNotExists(building)
            val place = dataGenerator.createPlace(placeName = SccRandom.string(32), building = building)
            val (placeAccessibility, buildingAccessibility) = dataGenerator.registerBuildingAndPlaceAccessibility(
                place = place, user = user,
                images = imageUrls.map { AccessibilityImage(it, it) }
            )
            Triple(user, placeAccessibility, buildingAccessibility)
        }
}
