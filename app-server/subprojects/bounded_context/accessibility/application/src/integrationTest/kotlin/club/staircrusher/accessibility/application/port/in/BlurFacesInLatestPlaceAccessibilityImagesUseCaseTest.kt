package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.persistence.AccessibilityImageFaceBlurringHistoryRepository
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.accessibility.domain.model.AccessibilityImage
import club.staircrusher.accessibility.domain.model.AccessibilityImageFaceBlurringHistory
import club.staircrusher.stdlib.testing.SccRandom
import club.staircrusher.testing.spring_it.mock.MockDetectFacesService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import java.time.Duration

class BlurFacesInLatestPlaceAccessibilityImagesUseCaseTest : BlurFacesITBase() {
    @Autowired
    private lateinit var accessibilityImageFaceBlurringHistoryRepository: AccessibilityImageFaceBlurringHistoryRepository

    @Autowired
    private lateinit var blurFacesInLatestPlaceAccessibilityImagesUseCase: BlurFacesInLatestPlaceAccessibilityImagesUseCase

    @Autowired
    private lateinit var buildingAccessibilityRepository: BuildingAccessibilityRepository

    @Autowired
    private lateinit var placeAccessibilityRepository: PlaceAccessibilityRepository

    @BeforeEach
    fun setUp() = runBlocking {
        val imageBytes = ByteArray(10) { it.toByte() }
        mockDetectFacesWithFaceImage(MockDetectFacesService.URL_WITH_FACES, imageBytes)
        mockDetectFacesWithNoFaceImage(MockDetectFacesService.URL_WITHOUT_FACES, imageBytes)
        Mockito.`when`(imageProcessor.blur(any(), any(), any())).thenReturn(imageBytes)

        placeAccessibilityRepository.deleteAll()
        buildingAccessibilityRepository.deleteAll()
        accessibilityImageFaceBlurringHistoryRepository.deleteAll()
    }

    @Test
    fun `얼굴 블러링 기록이 없으면 가장 오래된 place accessibility 의 이미지부터 얼굴 블러링한다`() {
        val (_, oldestPlaceAccessibility, _) = registerPlaceAccessibilityAndBuildingAccessibility(
            listOf(MockDetectFacesService.URL_WITH_FACES)
        )
        clock.advanceTime(Duration.ofMinutes(1))
        val (_, secondOldestPlaceAccessibility, _) = registerPlaceAccessibilityAndBuildingAccessibility(
            listOf(MockDetectFacesService.URL_WITH_FACES)
        )

        blurFacesInLatestPlaceAccessibilityImagesUseCase.handle()

        val blurredResult =
            accessibilityImageFaceBlurringHistoryRepository.findByPlaceAccessibilityId(oldestPlaceAccessibility.id)
        Assertions.assertTrue(blurredResult.isNotEmpty())
        val notBlurredResult =
            accessibilityImageFaceBlurringHistoryRepository.findByPlaceAccessibilityId(secondOldestPlaceAccessibility.id)
        Assertions.assertTrue(notBlurredResult.isEmpty())
    }

    @Test
    fun `얼굴 블러링 기록이 이후 가장 오래된 place accessibility 의 이미지부터 얼굴 블러링한다`() {
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
                    id = "",
                    placeAccessibilityId = oldestPlaceAccessibility.id,
                    buildingAccessibilityId = null,
                    originalImageUrls = listOf("image_url"),
                    blurredImageUrls = listOf("blurred_image_url"),
                    detectedPeopleCounts = emptyList(),
                    createdAt = clock.instant(),
                    updatedAt = clock.instant()
                )
            )
        }

        blurFacesInLatestPlaceAccessibilityImagesUseCase.handle()

        val result =
            accessibilityImageFaceBlurringHistoryRepository.findByPlaceAccessibilityId(secondOldestPlaceAccessibility.id)
        Assertions.assertTrue(result.isNotEmpty())
    }

    @Test
    fun `PlaceAccessibility 이미지 중 얼굴이 감지된 사진만 업데이트한다`() = runBlocking {
        val (_, placeAccessibility, _) = registerPlaceAccessibilityAndBuildingAccessibility(
            listOf(MockDetectFacesService.URL_WITH_FACES, MockDetectFacesService.URL_WITHOUT_FACES)
        )

        blurFacesInLatestPlaceAccessibilityImagesUseCase.handle()

        val reloadPlaceAccessibility = transactionManager.doInTransaction {
            placeAccessibilityRepository.findByIdOrNull(placeAccessibility.id)
        }
        val imageUrls = reloadPlaceAccessibility?.images?.map { it.imageUrl } ?: emptyList()
        Assertions.assertFalse(imageUrls.contains(MockDetectFacesService.URL_WITH_FACES))
        Assertions.assertTrue(imageUrls.contains(MockDetectFacesService.URL_WITHOUT_FACES))
    }

    @Test
    fun `이미 썸네일 처리가 된 PlaceAccessibility 의 경우 블러링 한 이미지 사용을 위해 썸네일 url을 제거한다`() = runBlocking {
        val (_, placeAccessibility, _) = registerPlaceAccessibilityAndBuildingAccessibility(
            listOf(MockDetectFacesService.URL_WITH_FACES, MockDetectFacesService.URL_WITHOUT_FACES)
        )

        blurFacesInLatestPlaceAccessibilityImagesUseCase.handle()

        val reloadPlaceAccessibility = transactionManager.doInTransaction {
            placeAccessibilityRepository.findByIdOrNull(placeAccessibility.id)
        }
        val images = reloadPlaceAccessibility?.images ?: emptyList()
        Assertions.assertTrue(images.isNotEmpty())
        Assertions.assertTrue(images.mapNotNull { it.thumbnailUrl }.isEmpty())
    }

    private fun registerPlaceAccessibilityAndBuildingAccessibility(imageUrls: List<String>) =
        transactionManager.doInTransaction {
            val user = testDataGenerator.createUser()
            val building = testDataGenerator.createBuilding()
            val place = testDataGenerator.createPlace(placeName = SccRandom.string(32), building = building)
            val (placeAccessibility, buildingAccessibility) = testDataGenerator.registerBuildingAndPlaceAccessibility(
                place = place, user = user, imageUrls = imageUrls, images = imageUrls.map { AccessibilityImage(it, it) }
            )
            Triple(user, placeAccessibility, buildingAccessibility)
        }
}
