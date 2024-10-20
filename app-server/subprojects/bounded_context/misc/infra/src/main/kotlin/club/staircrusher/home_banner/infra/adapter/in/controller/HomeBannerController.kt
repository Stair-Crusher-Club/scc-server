package club.staircrusher.home_banner.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.GetHomeBannersResponseDto
import club.staircrusher.home_banner.application.port.`in`.use_case.GetHomeBannersUseCase
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HomeBannerController(
    private val getHomeBannersUseCase: GetHomeBannersUseCase,
) {
    @PostMapping("/getHomeBanners")
    fun getHomeBanners(): GetHomeBannersResponseDto {
        return GetHomeBannersResponseDto(
            banners = getHomeBannersUseCase.handle().map { it.toDTO() },
        )
    }
}
