package club.staircrusher.place.infra.adapter.`in`.controller.accessibility

import club.staircrusher.place.application.port.`in`.accessibility.AccessibilityImageMigrationService
import club.staircrusher.place.application.port.out.accessibility.persistence.AccessibilityImageFaceBlurringHistoryRepository
import club.staircrusher.place.application.port.out.accessibility.persistence.AccessibilityImageRepository
import club.staircrusher.place.application.port.out.accessibility.persistence.BuildingAccessibilityRepository
import club.staircrusher.place.application.port.out.accessibility.persistence.PlaceAccessibilityRepository
import club.staircrusher.place.domain.model.accessibility.AccessibilityImage
import club.staircrusher.place.domain.model.accessibility.AccessibilityImageFaceBlurringHistory
import club.staircrusher.place.domain.model.accessibility.AccessibilityImageOld
import club.staircrusher.place.infra.adapter.`in`.controller.accessibility.base.AccessibilityITBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class AccessibilityImageMigrationTest : AccessibilityITBase() {

    @Autowired
    private lateinit var imageRepository: AccessibilityImageRepository

    @Autowired
    private lateinit var accessibilityImageMigrationService: AccessibilityImageMigrationService

    @Autowired
    private lateinit var placeAccessibilityRepository: PlaceAccessibilityRepository

    @Autowired
    private lateinit var buildingAccessibilityRepository: BuildingAccessibilityRepository

    @Autowired
    private lateinit var accessibilityImageFaceBlurringHistoryRepository: AccessibilityImageFaceBlurringHistoryRepository

    @BeforeEach
    fun setUp() = transactionManager.doInTransaction {
        imageRepository.deleteAll()
    }

    @Test
    fun `이미지 처리 - imageUrls 만 있는 경우`() {
        val place = testDataGenerator.createBuildingAndPlace()
        val user = testDataGenerator.createIdentifiedUser().account
        val (pa, ba) = testDataGenerator.registerBuildingAndPlaceAccessibility(place, user)
        transactionManager.doInTransaction {
            pa.oldImageUrls = listOf("pa.png")
            ba.elevatorImageUrls = listOf("baelev.png")
            ba.entranceImageUrls = listOf("baentrance.png")
            placeAccessibilityRepository.save(pa)
            buildingAccessibilityRepository.save(ba)
        }
        accessibilityImageMigrationService.migratePlaceAccessibility(pa.id)
        accessibilityImageMigrationService.migrateBuildingAccessibility(ba.id)
        transactionManager.doInTransaction {
            val placeImages = imageRepository.findByAccessibilityIdAndAccessibilityType(
                pa.id,
                AccessibilityImage.AccessibilityType.Place
            )
            val buildingImages = imageRepository.findByAccessibilityIdAndAccessibilityType(
                ba.id,
                AccessibilityImage.AccessibilityType.Building
            )
            assertEquals(1, placeImages.size)
            placeImages[0].let {
                assertEquals("pa.png", it.originalImageUrl)
                assertNull(it.thumbnailUrl)
                assertNull(it.blurredImageUrl)
                assertNull(it.imageType)
                assertNull(it.lastPostProcessedAt)
            }
            assertEquals(2, buildingImages.size)
            buildingImages[0].let {
                assertEquals("baelev.png", it.originalImageUrl)
                assertNull(it.thumbnailUrl)
                assertNull(it.blurredImageUrl)
                assertEquals(AccessibilityImage.ImageType.Elevator, it.imageType)
                assertNull(it.lastPostProcessedAt)
            }
            buildingImages[1].let {
                assertEquals("baentrance.png", it.originalImageUrl)
                assertNull(it.thumbnailUrl)
                assertNull(it.blurredImageUrl)
                assertEquals(AccessibilityImage.ImageType.Entrance, it.imageType)
                assertNull(it.lastPostProcessedAt)
            }
        }
    }

    @Test
    fun `이미지 처리 - images 있는 경우`() {
        val place = testDataGenerator.createBuildingAndPlace()
        val user = testDataGenerator.createIdentifiedUser().account
        val (pa, ba) = testDataGenerator.registerBuildingAndPlaceAccessibility(place, user)
        transactionManager.doInTransaction {
            pa.oldImageUrls = listOf("pa.png")
            pa.oldImages = listOf(AccessibilityImageOld("pa.png", "thumbnail.png"))
            ba.elevatorImageUrls = listOf("baelev.png")
            ba.elevatorImages = listOf(AccessibilityImageOld("baelev.png", "thumbnail.png"))
            ba.entranceImageUrls = listOf("baentrance.png")
            ba.entranceImages = listOf(AccessibilityImageOld("baentrance.png", "thumbnail.png"))
            placeAccessibilityRepository.save(pa)
            buildingAccessibilityRepository.save(ba)
        }
        accessibilityImageMigrationService.migratePlaceAccessibility(pa.id)
        accessibilityImageMigrationService.migrateBuildingAccessibility(ba.id)
        transactionManager.doInTransaction {
            val placeImages = imageRepository.findByAccessibilityIdAndAccessibilityType(
                pa.id,
                AccessibilityImage.AccessibilityType.Place
            )
            val buildingImages = imageRepository.findByAccessibilityIdAndAccessibilityType(
                ba.id,
                AccessibilityImage.AccessibilityType.Building
            )
            assertEquals(1, placeImages.size)
            placeImages[0].let {
                assertEquals("pa.png", it.originalImageUrl)
                assertEquals("thumbnail.png", it.thumbnailUrl)
                assertNull(it.blurredImageUrl)
                assertNull(it.imageType)
                assertNull(it.lastPostProcessedAt)
            }
            assertEquals(2, buildingImages.size)
            buildingImages[0].let {
                assertEquals("baelev.png", it.originalImageUrl)
                assertEquals("thumbnail.png", it.thumbnailUrl)
                assertNull(it.blurredImageUrl)
                assertEquals(AccessibilityImage.ImageType.Elevator, it.imageType)
                assertNull(it.lastPostProcessedAt)
            }
            buildingImages[1].let {
                assertEquals("baentrance.png", it.originalImageUrl)
                assertEquals("thumbnail.png", it.thumbnailUrl)
                assertNull(it.blurredImageUrl)
                assertEquals(AccessibilityImage.ImageType.Entrance, it.imageType)
                assertNull(it.lastPostProcessedAt)
            }
        }
    }

    @Test
    fun `이미지 처리 - images 와 blur history 있는 경우`() {
        val place = testDataGenerator.createBuildingAndPlace()
        val user = testDataGenerator.createIdentifiedUser().account
        val (pa, ba) = testDataGenerator.registerBuildingAndPlaceAccessibility(place, user)
        transactionManager.doInTransaction {
            pa.oldImageUrls = listOf("pablur.png")
            pa.oldImages = listOf(AccessibilityImageOld("pablur.png", "thumbnail.png"))
            accessibilityImageFaceBlurringHistoryRepository.save(
                AccessibilityImageFaceBlurringHistory(
                    id = UUID.randomUUID().toString(),
                    placeAccessibilityId = pa.id,
                    buildingAccessibilityId = null,
                    originalImageUrls = listOf("pa.png"),
                    blurredImageUrls = listOf("pablur.png"),
                    detectedPeopleCounts = listOf(1),
                )
            )
            ba.elevatorImageUrls = listOf("baelevblur.png")
            ba.elevatorImages = listOf(AccessibilityImageOld("baelevblur.png", "thumbnail.png"))
            ba.entranceImageUrls = listOf("baentranceblur.png")
            ba.entranceImages = listOf(AccessibilityImageOld("baentranceblur.png", "thumbnail.png"))
            accessibilityImageFaceBlurringHistoryRepository.save(
                AccessibilityImageFaceBlurringHistory(
                    id = UUID.randomUUID().toString(),
                    placeAccessibilityId = null,
                    buildingAccessibilityId = ba.id,
                    originalImageUrls = listOf("baelev.png", "baentrance.png"),
                    blurredImageUrls = listOf("baelevblur.png", "baentranceblur.png"),
                    detectedPeopleCounts = listOf(1, 1),
                )
            )
            placeAccessibilityRepository.save(pa)
            buildingAccessibilityRepository.save(ba)
        }
        accessibilityImageMigrationService.migratePlaceAccessibility(pa.id)
        accessibilityImageMigrationService.migrateBuildingAccessibility(ba.id)
        transactionManager.doInTransaction {
            val placeImages = imageRepository.findByAccessibilityIdAndAccessibilityType(
                pa.id,
                AccessibilityImage.AccessibilityType.Place
            )
            val buildingImages = imageRepository.findByAccessibilityIdAndAccessibilityType(
                ba.id,
                AccessibilityImage.AccessibilityType.Building
            )
            assertEquals(1, placeImages.size)
            placeImages[0].let {
                assertEquals("pa.png", it.originalImageUrl)
                assertEquals("thumbnail.png", it.thumbnailUrl)
                assertEquals("pablur.png", it.blurredImageUrl)
                assertNull(it.imageType)
            }
            assertEquals(2, buildingImages.size)
            buildingImages[0].let {
                assertEquals("baelev.png", it.originalImageUrl)
                assertEquals("thumbnail.png", it.thumbnailUrl)
                assertEquals("baelevblur.png", it.blurredImageUrl)
                assertEquals(AccessibilityImage.ImageType.Elevator, it.imageType)
            }
            buildingImages[1].let {
                assertEquals("baentrance.png", it.originalImageUrl)
                assertEquals("thumbnail.png", it.thumbnailUrl)
                assertEquals("baentranceblur.png", it.blurredImageUrl)
                assertEquals(AccessibilityImage.ImageType.Entrance, it.imageType)
            }
        }
    }
}
