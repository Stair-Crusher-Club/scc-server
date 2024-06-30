package club.staircrusher.quest.application.port.`in`

import club.staircrusher.accessibility.application.port.`in`.AccessibilityImageFaceBlurringService
import club.staircrusher.accessibility.application.port.`in`.AccessibilityImageFaceBlurringService.BlurResult
import club.staircrusher.accessibility.application.port.`in`.BlurFacesInLatestPlaceAccessibilityImagesUseCase
import club.staircrusher.accessibility.application.port.out.persistence.AccessibilityImageFaceBlurringHistoryRepository
import club.staircrusher.accessibility.domain.model.AccessibilityImageFaceBlurringHistory
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.stdlib.testing.SccRandom
import club.staircrusher.testing.spring_it.ITDataGenerator
import club.staircrusher.testing.spring_it.mock.MockSccClock
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import java.time.Duration

class BlurFacesInLatestPlaceAccessibilityImagesUseCaseTest {
    @Autowired
    private lateinit var accessibilityImageFaceBlurringHistoryRepository: AccessibilityImageFaceBlurringHistoryRepository

    @Autowired
    private lateinit var blurFacesInLatestPlaceAccessibilityImagesUseCase: BlurFacesInLatestPlaceAccessibilityImagesUseCase

    @Autowired
    private lateinit var clock: MockSccClock

    @Autowired
    private lateinit var dataGenerator: ITDataGenerator

    @Autowired
    private lateinit var transactionManager: TransactionManager

    @MockBean
    private lateinit var accessibilityImageFaceBlurringService: AccessibilityImageFaceBlurringService

    @BeforeEach
    fun setUpEach() {
        doDetectAndBlurFacesReturn()
        accessibilityImageFaceBlurringHistoryRepository.removeAll()
    }

    @Test
    fun `얼굴 블러링 기록이 없으면 가장 오래된 accessibility 의 이미지부터 얼굴 블러링한다`() {
        val (_, _, oldestAccessibility) = registerPlaceAccessibility()
        clock.advanceTime(Duration.ofMinutes(1))
        val (_, _, secondOldestAccessibility) = registerPlaceAccessibility()
        blurFacesInLatestPlaceAccessibilityImagesUseCase.handle()
        val blurredResult =
            accessibilityImageFaceBlurringHistoryRepository.findByPlaceAccessibilityId(oldestAccessibility.id)
        assertTrue(blurredResult.isNotEmpty())
        val notBlurredResult =
            accessibilityImageFaceBlurringHistoryRepository.findByPlaceAccessibilityId(secondOldestAccessibility.id)
        assertTrue(notBlurredResult.isNotEmpty())
    }

    @Test
    fun `얼굴 블러링 기록이 이후 가장 오래된 accessibility 의 이미지부터 얼굴 블러링한다`() {
        val (_, _, oldestAccessibility) = registerPlaceAccessibility()
        clock.advanceTime(Duration.ofMinutes(1))
        val (_, _, secondOldestAccessibility) = registerPlaceAccessibility()
        transactionManager.doInTransaction {
            accessibilityImageFaceBlurringHistoryRepository.save(
                AccessibilityImageFaceBlurringHistory(
                    id = "",
                    placeAccessibilityId = oldestAccessibility.id,
                    buildingAccessibilityId = null,
                    beforeImageUrl = "",
                    afterImageUrl = "",
                    detectedPeopleCount = null,
                    createdAt = clock.instant(),
                    updatedAt = clock.instant()
                )
            )
        }
        blurFacesInLatestPlaceAccessibilityImagesUseCase.handle()
        val result =
            accessibilityImageFaceBlurringHistoryRepository.findByPlaceAccessibilityId(secondOldestAccessibility.id)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `PlaceAccessibility 이미지 중 얼굴이 감지된 사진만 업데이트한다`() {
    }

    @Test
    fun `이미 썸네일 처리가 된 Accessibility 의 경우 블러링 한 이미지 사용을 위해 썸네일 url을 제거한다`() {
    }

    private fun registerPlaceAccessibility() = transactionManager.doInTransaction {
        val user = dataGenerator.createUser()
        val building = dataGenerator.createBuilding()
        dataGenerator.registerBuildingAccessibilityIfNotExists(building)
        val place = dataGenerator.createPlace(placeName = SccRandom.string(32), building = building)
        val placeAccessibility = dataGenerator.registerPlaceAccessibility(place = place, user = user)
        Triple(user, place, placeAccessibility)
    }

    private fun doDetectAndBlurFacesReturn() = runBlocking {
        Mockito.`when`(accessibilityImageFaceBlurringService.detectAndBlurFaces(listOf(URL_WITH_FACES)))
            .thenReturn(
                listOf(
                    BlurResult(
                        originalImageUrl = URL_WITH_FACES,
                        blurredImageUrl = BLURRED_URL_WITH_FACES,
                        detectedPeopleCount = 5
                    )
                )
            )
        Mockito.`when`(accessibilityImageFaceBlurringService.detectAndBlurFaces(listOf(URL_WITH_NO_FACES)))
            .thenReturn(
                listOf(
                    BlurResult(
                        originalImageUrl = URL_WITH_NO_FACES,
                        blurredImageUrl = URL_WITH_FACES,
                        detectedPeopleCount = 0
                    )
                )
            )
    }

    companion object {
        private const val URL_WITH_FACES = "https://staircrusher.club/faces.jpg"
        private const val BLURRED_URL_WITH_FACES = "https://staircrusher.club/faces.jpg"
        private const val URL_WITH_NO_FACES = "https://staircrusher.club/no_faces.jpg"
    }
}
