package club.staircrusher.place.infra.adapter.`in`.controller.accessibility

import club.staircrusher.place.application.port.`in`.accessibility.AccessibilityImageMigrationService
import club.staircrusher.place.application.port.`in`.accessibility.AccessibilityImagePipeline
import club.staircrusher.place.application.port.out.accessibility.persistence.AccessibilityImageRepository
import club.staircrusher.place.application.port.out.accessibility.persistence.PlaceAccessibilityRepository
import club.staircrusher.spring_web.security.InternalIpAddressChecker
import club.staircrusher.stdlib.persistence.TransactionManager
import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Duration
import java.time.Instant

@RestController
class AccessibilityImagePostProcessController(
    private val accessibilityImagePipeline: AccessibilityImagePipeline,
    private val accessibilityImageRepository: AccessibilityImageRepository,
    private val accessibilityImageMigrationService: AccessibilityImageMigrationService,
    private val transactionManager: TransactionManager,
    private val placeAccessibilityRepository: PlaceAccessibilityRepository,
    private val buildingAccessibilityRepository: PlaceAccessibilityRepository,
) {
    @PostMapping("/batchProcessUnprocessedAccessibilityImages")
    fun blurFacesInLatestPlaceAccessibilityImages(request: HttpServletRequest) {
        InternalIpAddressChecker.check(request)

        val targetImages = transactionManager.doInTransaction {
            accessibilityImageRepository.findFirst50ByLastPostProcessedTimeIsNullOrderByCreatedAtDesc()
        }
        runBlocking {
            accessibilityImagePipeline.postProcessImages(targetImages)
        }
    }

    @PostMapping("/migrateOldImagesToNewImages")
    fun migrateOldImages(
        request: HttpServletRequest,
        @RequestParam(required = false) start: Instant?,
        @RequestParam(required = false) target: String?,
    ) {
        InternalIpAddressChecker.check(request)
        val from = start ?: (Instant.now() - Duration.ofDays(2000))
        if (target == "place" || target == null) {
            val placeAccessibilityIds =
                transactionManager.doInTransaction { placeAccessibilityRepository.findMigrationTargets(from) }
            placeAccessibilityIds.forEach {
                accessibilityImageMigrationService.migratePlaceAccessibility(it)
            }
        }
        if (target == "building" || target == null) {
            val buildingAccessibilityIds =
                transactionManager.doInTransaction { buildingAccessibilityRepository.findMigrationTargets(from) }
            buildingAccessibilityIds.forEach {
                accessibilityImageMigrationService.migrateBuildingAccessibility(it)
            }
        }
    }
}
