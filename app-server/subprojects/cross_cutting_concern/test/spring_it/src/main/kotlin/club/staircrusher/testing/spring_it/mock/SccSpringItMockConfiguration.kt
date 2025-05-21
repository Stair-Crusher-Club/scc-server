package club.staircrusher.testing.spring_it.mock

import club.staircrusher.image.application.port.out.file_management.FileManagementService
import club.staircrusher.notification.port.out.PushSender
import club.staircrusher.place.application.port.`in`.accessibility.image.ImageBlurService
import club.staircrusher.place.application.port.`in`.accessibility.image.ImageThumbnailService
import club.staircrusher.place.application.port.`in`.accessibility.image.ImageFaceDetectionService
import club.staircrusher.place.application.port.`in`.accessibility.image.ImageInspectionService
import club.staircrusher.place.application.port.out.accessibility.SlackService
import club.staircrusher.place.application.port.out.place.web.MapsService
import club.staircrusher.quest.application.port.out.web.UrlShorteningService
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.user.application.port.out.web.login.kakao.KakaoLoginService
import club.staircrusher.user.application.port.out.web.subscription.StibeeSubscriptionService
import club.staircrusher.user.domain.service.ClientVersionService
import jakarta.annotation.Priority
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@AutoConfiguration
open class SccSpringItMockConfiguration {
    @Bean
    @ConditionalOnMissingBean(KakaoLoginService::class)
    open fun mockKakaoLoginService(): KakaoLoginService {
        return MockKakaoLoginService()
    }

    @Bean
    @Primary
    @Priority(1)
    open fun mockMapsService(): MapsService {
        return MockMapsService()
    }

    @Bean
    @Primary
    open fun mockPushSender(): PushSender {
        return MockPushSender()
    }

    @Bean
    @Primary
    open fun mockClientVersionService(): ClientVersionService {
        return MockClientVersionService()
    }

    @Bean
    @Primary
    open fun mockSccClock(): SccClock {
        return MockSccClock.getInstance()
    }

    @Bean
    @Primary
    open fun mockFileManagementService(): FileManagementService {
        return MockFileManagementService()
    }

    @Bean
    @Primary
    open fun mockThumbnailGenerator(): ImageThumbnailService {
        return MockImageThumbnailService()
    }

    @Bean
    @Primary
    open fun mockStibeeSubscriptionService(): StibeeSubscriptionService {
        return MockStibeeSubscriptionService()
    }

    @Bean
    @Primary
    open fun mockUrlShorteningService(): UrlShorteningService {
        return MockUrlShorteningService()
    }

    @Bean
    @Primary
    open fun mockDetectFacesService(): ImageFaceDetectionService {
        return MockImageFaceDetectionService()
    }

    @Bean
    @Primary
    open fun mockImageProcessor(): ImageBlurService {
        return MockImageBlurService()
    }

    @Bean
    @Primary
    open fun mockSlackService(): SlackService {
        return MockSlackService()
    }

    @Bean
    @Primary
    open fun mockImageInspectionService(): ImageInspectionService {
        return MockImageInspectionService()
    }
}
