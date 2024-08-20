package club.staircrusher.testing.spring_it.mock

import club.staircrusher.accessibility.application.port.`in`.image.ImageProcessor
import club.staircrusher.accessibility.application.port.`in`.image.ThumbnailGenerator
import club.staircrusher.accessibility.application.port.out.DetectFacesService
import club.staircrusher.accessibility.application.port.out.SlackService
import club.staircrusher.accessibility.application.port.out.file_management.FileManagementService
import club.staircrusher.place.application.port.out.web.MapsService
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
    open fun mockThumbnailGenerator(): ThumbnailGenerator {
        return MockThumbnailGenerator()
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
    open fun mockDetectFacesService(): DetectFacesService {
        return MockDetectFacesService()
    }

    @Bean
    @Primary
    open fun mockImageProcessor(): ImageProcessor {
        return MockImageProcessor()
    }

    @Bean
    @Primary
    open fun mockSlackService(): SlackService {
        return MockSlackService()
    }
}
