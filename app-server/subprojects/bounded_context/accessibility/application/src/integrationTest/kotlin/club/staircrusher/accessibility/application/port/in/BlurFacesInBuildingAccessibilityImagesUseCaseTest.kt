package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.`in`.image.ImageProcessor
import club.staircrusher.accessibility.application.port.out.DetectFacesResponse
import club.staircrusher.accessibility.application.port.out.DetectFacesService
import club.staircrusher.accessibility.application.port.out.persistence.AccessibilityImageFaceBlurringHistoryRepository
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.accessibility.domain.model.AccessibilityImage
import club.staircrusher.stdlib.Rect
import club.staircrusher.stdlib.Size
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.stdlib.testing.SccRandom
import club.staircrusher.testing.spring_it.ITDataGenerator
import club.staircrusher.testing.spring_it.base.SccSpringITApplication
import club.staircrusher.testing.spring_it.mock.MockDetectFacesService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

@SpringBootTest(classes = [SccSpringITApplication::class])
class BlurFacesInBuildingAccessibilityImagesUseCaseTest {
    @Autowired
    private lateinit var transactionManager: TransactionManager

    @Autowired
    private lateinit var dataGenerator: ITDataGenerator

    @Autowired
    private lateinit var accessibilityImageFaceBlurringHistoryRepository: AccessibilityImageFaceBlurringHistoryRepository

    @Autowired
    private lateinit var placeAccessibilityRepository: PlaceAccessibilityRepository

    @Autowired
    private lateinit var buildingAccessibilityRepository: BuildingAccessibilityRepository

    @Autowired
    private lateinit var blurFacesInBuildingAccessibilityImagesUseCase: BlurFacesInBuildingAccessibilityImagesUseCase

    @MockBean
    private lateinit var imageProcessor: ImageProcessor

    @MockBean
    private lateinit var detectFacesService: DetectFacesService


    @BeforeEach
    fun setUp() = runBlocking {
        val imageBytes = ByteArray(10) { it.toByte() }
        Mockito.`when`(detectFacesService.detect(eq(MockDetectFacesService.URL_WITH_FACES))).thenReturn(
            DetectFacesResponse(
                imageBytes = imageBytes, imageSize = Size(100, 100), positions = listOf(Rect(0, 0, 10, 10))
            )
        )
        Mockito.`when`(detectFacesService.detect(eq(MockDetectFacesService.URL_WITHOUT_FACES))).thenReturn(
            DetectFacesResponse(
                imageBytes = imageBytes, imageSize = Size(100, 100), positions = emptyList()
            )
        )
        Mockito.`when`(imageProcessor.blur(any(), any())).thenReturn(imageBytes)

        placeAccessibilityRepository.removeAll()
        buildingAccessibilityRepository.removeAll()
        accessibilityImageFaceBlurringHistoryRepository.removeAll()
    }

    @Test
    fun `BuildingAccessibility 이미지 중 얼굴이 감지된 사진만 업데이트한다`() {
        val (_, _, buildingAccessibility) = registerPlaceAccessibilityAndBuildingAccessibility(
            listOf(MockDetectFacesService.URL_WITH_FACES, MockDetectFacesService.URL_WITHOUT_FACES)
        )

        blurFacesInBuildingAccessibilityImagesUseCase.handle(buildingAccessibilityId = buildingAccessibility.id)

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
    fun `이미 썸네일 처리가 된 BuildingAccessibility 의 경우 블러링 한 이미지 사용을 위해 썸네일 url을 제거한다`() {
        val (_, _, buildingAccessibility) = registerPlaceAccessibilityAndBuildingAccessibility(
            imageUrls = listOf(MockDetectFacesService.URL_WITH_FACES, MockDetectFacesService.URL_WITHOUT_FACES)
        )

        blurFacesInBuildingAccessibilityImagesUseCase.handle(buildingAccessibilityId = buildingAccessibility.id)

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
            val user = dataGenerator.createUser()
            val building = dataGenerator.createBuilding()
            val place = dataGenerator.createPlace(placeName = SccRandom.string(32), building = building)
            val (placeAccessibility, buildingAccessibility) = dataGenerator.registerBuildingAndPlaceAccessibility(
                place = place, user = user, imageUrls = imageUrls, images = imageUrls.map { AccessibilityImage(it, it) }
            )
            Triple(user, placeAccessibility, buildingAccessibility)
        }
}
