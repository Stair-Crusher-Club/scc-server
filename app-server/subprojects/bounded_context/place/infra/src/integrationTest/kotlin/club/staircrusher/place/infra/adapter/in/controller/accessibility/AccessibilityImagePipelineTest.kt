package club.staircrusher.place.infra.adapter.`in`.controller.accessibility

import club.staircrusher.place.application.port.`in`.accessibility.AccessibilityImagePipeline
import club.staircrusher.place.application.port.out.accessibility.persistence.AccessibilityImageRepository
import club.staircrusher.place.domain.model.accessibility.AccessibilityImage
import club.staircrusher.place.infra.adapter.`in`.controller.accessibility.base.AccessibilityITBase
import club.staircrusher.stdlib.clock.SccClock
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration

class AccessibilityImagePipelineTest : AccessibilityITBase() {

    @Autowired
    private lateinit var imageRepository: AccessibilityImageRepository

    @Autowired
    private lateinit var accessibilityImagePipeline: AccessibilityImagePipeline

    @BeforeEach
    fun setUp() = transactionManager.doInTransaction {
        imageRepository.deleteAll()
    }

    @Disabled
    @Test
    fun `이미지 처리`() {
        val fixedAccessibilityId = "someidnotused!"
        val savedImages = transactionManager.doInTransaction {
            imageRepository.saveAll(
                (0 until 10).map {
                    AccessibilityImage(
                        accessibilityId = fixedAccessibilityId,
                        accessibilityType = AccessibilityImage.AccessibilityType.Building,
                        originalImageUrl = "url/example$it.png",
                    )
                }
            )
        }.toList()

        runBlocking {
            accessibilityImagePipeline.postProcessImages(savedImages)
        }

        transactionManager.doInTransaction {
            val result = imageRepository.findByAccessibilityIdAndAccessibilityType(
                fixedAccessibilityId,
                AccessibilityImage.AccessibilityType.Building
            ).toList()
            assertEquals(10, result.size)
            result.forEach {
                assertNotNull(it.lastPostProcessedAt)
                assertNotNull(it.thumbnailUrl)
                assertNotNull(it.blurredImageUrl)
            }
        }
    }

    @Test
    fun `배치는 최대 10개의 이미지를 가져온다`() {
        val savedImages = transactionManager.doInTransaction {
            imageRepository.saveAll(
                (0 until 20).map {
                    AccessibilityImage(
                        accessibilityId = "temp$it",
                        accessibilityType = AccessibilityImage.AccessibilityType.Building,
                        originalImageUrl = "url/example$it.png",
                    )
                }
            )
        }
        val retrievedImages = transactionManager.doInTransaction {
            accessibilityImagePipeline.getTargetImages()
        }
        assertEquals(10, retrievedImages.size)
    }

    @Test
    fun `배치는 처리되지 않은 이미지들만 생성된 역순으로 가져온다`() {
        val now = SccClock.instant()
        val savedImages = transactionManager.doInTransaction {
            imageRepository.saveAll(
                listOf(
                    AccessibilityImage(
                        accessibilityId = "temp",
                        accessibilityType = AccessibilityImage.AccessibilityType.Building,
                        originalImageUrl = "url/processed_example.png",
                        lastPostProcessedAt = SccClock.instant() + Duration.ofDays(400),
                    ),
                    AccessibilityImage(
                        accessibilityId = "temp",
                        accessibilityType = AccessibilityImage.AccessibilityType.Building,
                        originalImageUrl = "url/unprocessed_old_example.png",
                    ).also {
                        it.createdAt = now
                    },
                    AccessibilityImage(
                        accessibilityId = "temp",
                        accessibilityType = AccessibilityImage.AccessibilityType.Building,
                        originalImageUrl = "url/unprocessed_new_example.png",
                    ).also {
                        it.createdAt = now + Duration.ofDays(1)
                    },
                )
            )
        }

        val retrievedImages = transactionManager.doInTransaction {
            accessibilityImagePipeline.getTargetImages()
        }
        assertEquals(2, retrievedImages.size)
        assertEquals("url/unprocessed_new_example.png", retrievedImages[0].originalImageUrl)
        assertEquals("url/unprocessed_old_example.png", retrievedImages[1].originalImageUrl)
    }
}
