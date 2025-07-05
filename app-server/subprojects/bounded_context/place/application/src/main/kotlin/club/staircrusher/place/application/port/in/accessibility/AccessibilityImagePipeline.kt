package club.staircrusher.place.application.port.`in`.accessibility

import club.staircrusher.place.application.port.out.accessibility.persistence.AccessibilityImageRepository
import club.staircrusher.place.domain.model.accessibility.AccessibilityImage
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.util.concurrent.Executors

@Component
class AccessibilityImagePipeline(
    private val accessibilityImageFaceBlurringService: AccessibilityImageFaceBlurringService,
    private val accessibilityImageThumbnailService: AccessibilityImageThumbnailService,
    private val accessibilityImageRepository: AccessibilityImageRepository,
    private val transactionManager: TransactionManager,
) {
    private val taskExecutor = Executors.newCachedThreadPool()

    suspend fun postProcessImages(images: List<AccessibilityImage>) {
        val processedImages = images
            .let { accessibilityImageFaceBlurringService.blurImages(it) }
            .let { accessibilityImageThumbnailService.generateThumbnails(it) }
            .also { it.forEach { img -> img.lastPostProcessedAt = SccClock.instant() } }
        transactionManager.doInTransaction {
            accessibilityImageRepository.saveAll(processedImages)
        }
    }

    fun asyncPostProcessImages(images: List<AccessibilityImage>) {
        transactionManager.doAfterCommit {
            taskExecutor.submit {
                runBlocking {
                    postProcessImages(images)
                }
            }
        }
    }

    fun getTargetImages(): List<AccessibilityImage> {
        return transactionManager.doInTransaction {
            accessibilityImageRepository.findBatchTargetsBefore(썸네일_블러_최초마이그레이션시점)
        }
    }

    companion object {
        // 실섭 배포 시점에 맞추어 업데이트할 예정
        val 썸네일_블러_최초마이그레이션시점 = Instant.parse("2025-06-30T00:00:00Z")
    }
}
