package club.staircrusher.home_banner.application.port.`in`.use_case

import club.staircrusher.home_banner.application.port.out.persistence.BannerRepository
import club.staircrusher.home_banner.domain.model.Banner
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.persistence.TransactionManager
import java.time.Instant

@Component
class AdminCreateBannerUseCase(
    private val transactionManager: TransactionManager,
    private val bannerRepository: BannerRepository,
) {
    fun handle(
        loggingKey: String,
        imageUrl: String,
        clickPageUrl: String,
        clickPageTitle: String,
        startAtMillis: Long?,
        endAtMillis: Long?,
        displayOrder: Int,
    ) : Banner = transactionManager.doInTransaction {
        Banner(
            id = EntityIdGenerator.generateRandom(),
            loggingKey = loggingKey,
            imageUrl = imageUrl,
            clickPageUrl = clickPageUrl,
            clickPageTitle = clickPageTitle,
            startAt = startAtMillis?.let { Instant.ofEpochMilli(it) },
            endAt = endAtMillis?.let { Instant.ofEpochMilli(it) },
            displayOrder = displayOrder,
        )
            .also { bannerRepository.save(it) }
    }
}
