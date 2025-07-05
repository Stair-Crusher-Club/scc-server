package club.staircrusher.place.infra.adapter.`in`.controller.accessibility

import club.staircrusher.place.application.port.`in`.accessibility.AccessibilityImageMigrationService
import club.staircrusher.place.application.port.`in`.accessibility.AccessibilityImagePipeline
import club.staircrusher.place.application.port.out.accessibility.persistence.BuildingAccessibilityRepository
import club.staircrusher.place.application.port.out.accessibility.persistence.PlaceAccessibilityRepository
import club.staircrusher.spring_web.security.InternalIpAddressChecker
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.persistence.TransactionManager
import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Duration
import java.time.Instant
import java.util.concurrent.Executors

@RestController
class AccessibilityImagePostProcessController(
    private val accessibilityImagePipeline: AccessibilityImagePipeline,
    private val accessibilityImageMigrationService: AccessibilityImageMigrationService,
    private val transactionManager: TransactionManager,
    private val placeAccessibilityRepository: PlaceAccessibilityRepository,
    private val buildingAccessibilityRepository: BuildingAccessibilityRepository,
) {
    private val taskExecutor1 = Executors.newSingleThreadExecutor()
    private val taskExecutor2 = Executors.newSingleThreadExecutor()
    private val logger = KotlinLogging.logger {}

    @PostMapping("/batchProcessUnprocessedAccessibilityImages")
    fun blurFacesInLatestPlaceAccessibilityImages(request: HttpServletRequest) {
        InternalIpAddressChecker.check(request)

        val targetImages = accessibilityImagePipeline.getTargetImages()
        taskExecutor1.submit {
            targetImages.forEach {
                runBlocking {
                    accessibilityImagePipeline.postProcessImages(listOf(it))
                }
            }
        }
    }

    @PostMapping("/migrateOldImagesToNewImages")
    fun migrateOldImages(
        request: HttpServletRequest,
        @RequestParam(required = false) start: Instant?,
        @RequestParam(required = false) target: String?,
    ) {
        InternalIpAddressChecker.check(request)

        taskExecutor2.submit {
            val from = start ?: (SccClock.instant() - Duration.ofDays(2000))

            fun logMemoryUsage(label: String) {
                val runtime = Runtime.getRuntime()
                val used = (runtime.totalMemory() - runtime.freeMemory()) / (1024.0 * 1024.0)
                logger.info("[$label] Memory used: $used MB")
            }

            val logInterval = 1000

            if (target == "place" || target == null) {
                val placeAccessibilityIds =
                    transactionManager.doInTransaction { placeAccessibilityRepository.findMigrationTargets(from) }
                logger.info("[place] Start migration for ${placeAccessibilityIds.size} migrations")
                logMemoryUsage("place start")
                placeAccessibilityIds.forEachIndexed { index, id ->
                    accessibilityImageMigrationService.migratePlaceAccessibility(id)
                    if ((index + 1) % logInterval == 0) {
                        logger.info("[place] Progress: ${index + 1} processed")
                        logMemoryUsage("place $id")
                        System.gc()
                    }
                }
                logger.info("[place] Finished migration for ${placeAccessibilityIds.size} migrations")
            }
            if (target == "building" || target == null) {
                val buildingAccessibilityIds =
                    transactionManager.doInTransaction { buildingAccessibilityRepository.findMigrationTargets(from) }
                logger.info("[building] Start migration for ${buildingAccessibilityIds.size} migrations")
                logMemoryUsage("building start")
                buildingAccessibilityIds.forEachIndexed { index, id ->
                    accessibilityImageMigrationService.migrateBuildingAccessibility(id)
                    if ((index + 1) % logInterval == 0) {
                        logger.info("[building] Progress: ${index + 1} processed")
                        logMemoryUsage("building $id")
                        System.gc()
                    }
                }
                logger.info("[building] Finished migration for ${buildingAccessibilityIds.size} migrations")
            }
        }
    }
}
