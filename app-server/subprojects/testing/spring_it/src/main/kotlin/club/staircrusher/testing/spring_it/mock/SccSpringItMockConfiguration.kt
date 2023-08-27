package club.staircrusher.testing.spring_it.mock

import club.staircrusher.place.application.port.out.web.MapsService
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.user.application.port.out.web.KakaoLoginService
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
    open fun mockMapsService(): MapsService {
        return MockMapsService()
    }

    @Bean
    @Primary
    open fun mockSccClock(): SccClock {
        return MockSccClock()
    }
}
