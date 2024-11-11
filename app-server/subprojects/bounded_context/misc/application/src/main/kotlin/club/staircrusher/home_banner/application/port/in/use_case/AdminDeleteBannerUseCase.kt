package club.staircrusher.home_banner.application.port.`in`.use_case

import club.staircrusher.home_banner.application.port.out.persistence.BannerRepository
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class AdminDeleteBannerUseCase(
    private val transactionManager: TransactionManager,
    private val bannerRepository: BannerRepository,
) {
    fun handle(bannerId: String) = transactionManager.doInTransaction {
        bannerRepository.deleteById(bannerId)
    }
}
