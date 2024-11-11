package club.staircrusher.home_banner.application.port.`in`.use_case

import club.staircrusher.home_banner.application.port.out.persistence.BannerRepository
import club.staircrusher.home_banner.domain.model.Banner
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class GetHomeBannersUseCase(
    private val transactionManager: TransactionManager,
    private val bannerRepository: BannerRepository,
) {
    fun handle(): List<Banner> = transactionManager.doInTransaction {
        bannerRepository.findActiveHomeBanners(SccClock.instant(), Banner.displaySort)
    }
}
