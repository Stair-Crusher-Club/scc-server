package club.staircrusher.place.infra.adapter.`in`.controller.accessibility

import club.staircrusher.place.application.port.`in`.accessibility.AccessibilityImageMigrationService
import club.staircrusher.place.application.port.`in`.accessibility.AccessibilityImagePipeline
import club.staircrusher.spring_web.security.InternalIpAddressChecker
import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.Executors

@RestController
class AccessibilityImagePostProcessController(
    private val accessibilityImagePipeline: AccessibilityImagePipeline,
    private val accessibilityImageMigrationService: AccessibilityImageMigrationService,
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
        @RequestParam(required = false) target: String?,
        @RequestBody body: List<String>
    ) {
        InternalIpAddressChecker.check(request)

        taskExecutor2.submit {
            fun logMemoryUsage(label: String) {
                val runtime = Runtime.getRuntime()
                val used = (runtime.totalMemory() - runtime.freeMemory()) / (1024.0 * 1024.0)
                logger.info("[$label] Memory used: $used MB")
            }

            val logInterval = 1000

            if (target == "place" || target == null) {
                val placeAccessibilityIds = body
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
                val buildingAccessibilityIds = body
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
