package club.staircrusher.quest.application.port.`in`

import club.staircrusher.accessibility.application.port.`in`.AccessibilityImageFaceBlurringService
import club.staircrusher.accessibility.application.port.`in`.BlurFacesInLatestPlaceAccessibilityImagesUseCase
import club.staircrusher.accessibility.application.port.out.persistence.AccessibilityImageFaceBlurringHistoryRepository
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.testing.spring_it.ITDataGenerator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean

class BlurFacesInLatestBuildingAccessibilityImagesUseCaseTest {
    @Autowired
    private lateinit var accessibilityImageFaceBlurringHistoryRepository: AccessibilityImageFaceBlurringHistoryRepository

    @Autowired
    private lateinit var blurFacesInLatestPlaceAccessibilityImagesUseCase: BlurFacesInLatestPlaceAccessibilityImagesUseCase

    @Autowired
    private lateinit var dataGenerator: ITDataGenerator

    @Autowired
    private lateinit var transactionManager: TransactionManager

    @MockBean
    private lateinit var accessibilityImageFaceBlurringService: AccessibilityImageFaceBlurringService

    @BeforeEach
    fun setUp() {
        accessibilityImageFaceBlurringService
//        Mockito.`when`(kakaoLoginService.parseIdToken("dummy")).thenReturn(
//            KakaoIdToken(
//                issuer = "https://kauth.kakao.com",
//                audience = "clientId",
//                expiresAtEpochSecond = SccClock.instant().epochSecond + 10,
//                kakaoSyncUserId = "kakaoSyncUserId1",
//            )
//        )
    }

    @Test
    fun `얼굴 블러링 기록이 없으면 가장 오래된 accessibility 의 이미지부터 얼굴 블러링한다`() {
    }

    @Test
    fun `얼굴 블러링 기록이 이후 가장 오래된 accessibility 의 이미지부터 얼굴 블러링한다`() {
    }

    @Test
    fun `BuildingAccessibility 이미지 중 얼굴이 감지된 사진만 업데이트한다`() {
    }

    @Test
    fun `이미 썸네일 처리가 된 Accessibility 의 경우 블러링 한 이미지 사용을 위해 썸네일 url을 제거한다`() {
    }
}
