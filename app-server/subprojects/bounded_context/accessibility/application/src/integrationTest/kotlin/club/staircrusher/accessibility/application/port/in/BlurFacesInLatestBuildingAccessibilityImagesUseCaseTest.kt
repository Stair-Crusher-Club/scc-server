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

class BlurFacesInLatestBuildingAccessibilityImagesUseCaseTest : BlurFacesITBase() {
    @Autowired
    private lateinit var accessibilityImageFaceBlurringHistoryRepository: AccessibilityImageFaceBlurringHistoryRepository

    @Autowired
    private lateinit var blurFacesInLatestBuildingAccessibilityImagesUseCase: BlurFacesInLatestBuildingAccessibilityImagesUseCase

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
    fun `얼굴 블러링 기록이 없으면 가장 오래된 building accessibility 의 이미지부터 얼굴 블러링한다`() {
        val (_, _, oldestBuildingAccessibility) = registerPlaceAccessibilityAndBuildingAccessibility(
            listOf(MockDetectFacesService.URL_WITH_FACES)
        )
        clock.advanceTime(Duration.ofMinutes(1))
        val (_, _, secondOldestBuildingAccessibility) = registerPlaceAccessibilityAndBuildingAccessibility(
            listOf(MockDetectFacesService.URL_WITH_FACES)
        )

        blurFacesInLatestBuildingAccessibilityImagesUseCase.handle()

        val blurredResult = accessibilityImageFaceBlurringHistoryRepository.findByBuildingAccessibilityId(
            oldestBuildingAccessibility.id
        )
        Assertions.assertTrue(blurredResult.isNotEmpty())
        val notBlurredResult = accessibilityImageFaceBlurringHistoryRepository.findByBuildingAccessibilityId(
            secondOldestBuildingAccessibility.id
        )
        Assertions.assertTrue(notBlurredResult.isEmpty())
    }

    @Test
    fun `얼굴 블러링 기록이 이후 가장 오래된 building accessibility 의 이미지부터 얼굴 블러링한다`() {
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
                    originalImageUrls = listOf("image_url"),
                    blurredImageUrls = listOf("blurred_image_url"),
                    detectedPeopleCounts = emptyList(),
                    createdAt = clock.instant(),
                    updatedAt = clock.instant()
                )
            )
        }

        blurFacesInLatestBuildingAccessibilityImagesUseCase.handle()

        val result = accessibilityImageFaceBlurringHistoryRepository.findByBuildingAccessibilityId(
            secondOldestBuildingAccessibility.id
        )
        Assertions.assertTrue(result.isNotEmpty())
    }

    @Test
    fun `BuildingAccessibility 이미지 중 얼굴이 감지된 사진만 업데이트한다`() = runBlocking {
        val (_, _, buildingAccessibility) = registerPlaceAccessibilityAndBuildingAccessibility(
            listOf(MockDetectFacesService.URL_WITH_FACES, MockDetectFacesService.URL_WITHOUT_FACES)
        )

        blurFacesInLatestBuildingAccessibilityImagesUseCase.handle()

        val reloadBuildingAccessibility = transactionManager.doInTransaction {
            buildingAccessibilityRepository.findByIdOrNull(buildingAccessibility.id)
        }
        val entranceImages = reloadBuildingAccessibility?.entranceImages?.map { it.imageUrl } ?: emptyList()
        Assertions.assertFalse(entranceImages.contains(MockDetectFacesService.URL_WITH_FACES))
        Assertions.assertTrue(entranceImages.contains(MockDetectFacesService.URL_WITHOUT_FACES))
        val elevatorImages = reloadBuildingAccessibility?.entranceImages?.map { it.imageUrl } ?: emptyList()
        Assertions.assertFalse(elevatorImages.contains(MockDetectFacesService.URL_WITH_FACES))
        Assertions.assertTrue(elevatorImages.contains(MockDetectFacesService.URL_WITHOUT_FACES))
    }


    @Test
    fun `이미 썸네일 처리가 된 BuildingAccessibility 의 경우 블러링 한 이미지 사용을 위해 썸네일 url을 제거한다`() = runBlocking {
        val (_, _, buildingAccessibility) = registerPlaceAccessibilityAndBuildingAccessibility(
            imageUrls = listOf(MockDetectFacesService.URL_WITH_FACES, MockDetectFacesService.URL_WITHOUT_FACES)
        )

        blurFacesInLatestBuildingAccessibilityImagesUseCase.handle()

        val reloadBuildingAccessibility = transactionManager.doInTransaction {
            buildingAccessibilityRepository.findByIdOrNull(buildingAccessibility.id)
        }
        val entranceImages = reloadBuildingAccessibility?.entranceImages ?: emptyList()
        Assertions.assertTrue(entranceImages.isNotEmpty())
        Assertions.assertTrue(entranceImages.mapNotNull { it.thumbnailUrl }.isEmpty())
        val elevatorImages = reloadBuildingAccessibility?.entranceImages ?: emptyList()
        Assertions.assertTrue(elevatorImages.isNotEmpty())
        Assertions.assertTrue(elevatorImages.mapNotNull { it.thumbnailUrl }.isEmpty())
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
