package club.staircrusher.place.infra.adapter.`in`.controller.accessibility

import club.staircrusher.place.application.port.`in`.accessibility.BlurFacesInLatestBuildingAccessibilityImagesUseCase
import club.staircrusher.place.application.port.out.accessibility.persistence.AccessibilityImageFaceBlurringHistoryRepository
import club.staircrusher.place.application.port.out.accessibility.persistence.BuildingAccessibilityRepository
import club.staircrusher.place.application.port.out.accessibility.persistence.PlaceAccessibilityRepository
import club.staircrusher.place.domain.model.accessibility.AccessibilityImage
import club.staircrusher.place.domain.model.accessibility.AccessibilityImageFaceBlurringHistory
import club.staircrusher.place.infra.adapter.`in`.controller.accessibility.base.BlurFacesITBase
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

class BlurFacesInBuildingAccessibilityImagesTest : BlurFacesITBase() {
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
    fun `얼굴 블러링 기록이 있는 경우 그 이후 가장 오래된 building accessibility 의 이미지부터 얼굴 블러링한다`() {
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
    fun `얼굴 블러링 기록에 있는 building accessibility 가 hard delete 된 경우에도 잘 작동한다`() {
        val (_, _, firstBuildingAccessibility) = registerPlaceAccessibilityAndBuildingAccessibility(
            listOf(MockDetectFacesService.URL_WITH_FACES)
        )
        clock.advanceTime(Duration.ofMinutes(1))
        val (_, _, secondBuildingAccessibility) = registerPlaceAccessibilityAndBuildingAccessibility(
            listOf(MockDetectFacesService.URL_WITH_FACES)
        )
        clock.advanceTime(Duration.ofMinutes(1))
        val (_, _, thirdBuildingAccessibility) = registerPlaceAccessibilityAndBuildingAccessibility(
            listOf(MockDetectFacesService.URL_WITH_FACES)
        )
        transactionManager.doInTransaction {
            accessibilityImageFaceBlurringHistoryRepository.save(
                AccessibilityImageFaceBlurringHistory(
                    id = "", placeAccessibilityId = null,
                    buildingAccessibilityId = firstBuildingAccessibility.id,
                    originalImageUrls = listOf("image_url"),
                    blurredImageUrls = listOf("blurred_image_url"),
                    detectedPeopleCounts = emptyList(),
                )
            )
        }

        transactionManager.doInTransaction {
            buildingAccessibilityRepository.deleteById(firstBuildingAccessibility.id)
        }

        blurFacesInLatestBuildingAccessibilityImagesUseCase.handle()
        run {
            val result = accessibilityImageFaceBlurringHistoryRepository.findByBuildingAccessibilityId(
                secondBuildingAccessibility.id
            )
            Assertions.assertTrue(result.isNotEmpty())
        }

        // Run blur again
        blurFacesInLatestBuildingAccessibilityImagesUseCase.handle()
        run {
            val result = accessibilityImageFaceBlurringHistoryRepository.findByBuildingAccessibilityId(
                thirdBuildingAccessibility.id
            )
            Assertions.assertTrue(result.isNotEmpty())
        }
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

    @Test
    fun `모든 데이터를 처리한 상황이면 블러링을 하지 않는다`() = runBlocking {
        val (_, _, buildingAccessibility1) = registerPlaceAccessibilityAndBuildingAccessibility(
            imageUrls = listOf(MockDetectFacesService.URL_WITH_FACES, MockDetectFacesService.URL_WITHOUT_FACES)
        )
        clock.advanceTime(Duration.ofMillis(1))
        val (_, _, buildingAccessibility2) = registerPlaceAccessibilityAndBuildingAccessibility(
            imageUrls = listOf(MockDetectFacesService.URL_WITH_FACES, MockDetectFacesService.URL_WITHOUT_FACES)
        )

        blurFacesInLatestBuildingAccessibilityImagesUseCase.handle()
        blurFacesInLatestBuildingAccessibilityImagesUseCase.handle()
        blurFacesInLatestBuildingAccessibilityImagesUseCase.handle()

        Assertions.assertTrue(accessibilityImageFaceBlurringHistoryRepository.findByBuildingAccessibilityId(buildingAccessibility1.id).count() == 1)
        Assertions.assertTrue(accessibilityImageFaceBlurringHistoryRepository.findByBuildingAccessibilityId(buildingAccessibility1.id).count() == 1)
    }

    private fun registerPlaceAccessibilityAndBuildingAccessibility(imageUrls: List<String>) =
        transactionManager.doInTransaction {
            val (userAccount, _) = testDataGenerator.createIdentifiedUser()
            val building = testDataGenerator.createBuilding()
            val place = testDataGenerator.createPlace(placeName = SccRandom.string(32), building = building)
            val (placeAccessibility, buildingAccessibility) = testDataGenerator.registerBuildingAndPlaceAccessibility(
                place = place, userAccount = userAccount, imageUrls = imageUrls, images = imageUrls.map { AccessibilityImage(it, it) }
            )
            Triple(userAccount, placeAccessibility, buildingAccessibility)
        }
}
