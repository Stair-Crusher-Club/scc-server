package club.staircrusher.home_banner.application.port.`in`.use_case

import club.staircrusher.home_banner.application.port.out.persistence.HomeBannerRepository
import club.staircrusher.home_banner.domain.model.HomeBanner
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class GetHomeBannersUseCase(
    private val transactionManager: TransactionManager,
    private val homeBannerRepository: HomeBannerRepository,
) {
    fun handle(): List<HomeBanner> = transactionManager.doInTransaction {
        homeBannerRepository.findActiveHomeBanners(SccClock.instant())
    }
}
