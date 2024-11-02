package club.staircrusher.home_banner.infra.adapter.`in`.controller

import club.staircrusher.admin_api.spec.dto.AdminCreateBannerRequestDTO
import club.staircrusher.admin_api.spec.dto.AdminListAllBannersResponseDTO
import club.staircrusher.admin_api.spec.dto.AdminListHomeBannersResponseDTO
import club.staircrusher.home_banner.application.port.`in`.use_case.AdminCreateBannerUseCase
import club.staircrusher.home_banner.application.port.`in`.use_case.AdminDeleteBannerUseCase
import club.staircrusher.home_banner.application.port.`in`.use_case.AdminListBannersUseCase
import club.staircrusher.home_banner.application.port.`in`.use_case.GetHomeBannersUseCase
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AdminBannerController(
    private val adminListBannersUseCase: AdminListBannersUseCase,
    private val getHomeBannersUseCase: GetHomeBannersUseCase,
    private val adminCreateBannerUseCase: AdminCreateBannerUseCase,
    private val adminDeleteBannerUseCase: AdminDeleteBannerUseCase,

) {
    @GetMapping("/admin/banners")
    fun adminListAllBanners(): AdminListAllBannersResponseDTO {
        val activeBanners = adminListBannersUseCase.handle()
        return AdminListAllBannersResponseDTO(
            banners = activeBanners.map { it.toAdminDTO() }
        )
    }

    @GetMapping("/admin/banners/home-banner")
    fun adminListHomeBanners(): AdminListHomeBannersResponseDTO {
        val activeBanners = getHomeBannersUseCase.handle()
        return AdminListHomeBannersResponseDTO(
            banners = activeBanners.map { it.toAdminDTO() }
        )
    }

    @PostMapping("/admin/banners")
    fun adminCreateBanner(@RequestBody request: AdminCreateBannerRequestDTO) {
        adminCreateBannerUseCase.handle(
            loggingKey = request.loggingKey,
            imageUrl = request.imageUrl,
            clickPageUrl = request.clickPageUrl,
            clickPageTitle = request.clickPageTitle,
            startAtMillis = request.startAt?.value,
            endAtMillis = request.endAt?.value,
            displayOrder = request.displayOrder,
        )
    }

    @DeleteMapping("/admin/banners/{bannerId}")
    fun adminDeleteBanner(@PathVariable bannerId: String) {
        adminDeleteBannerUseCase.handle(bannerId)
    }
}
