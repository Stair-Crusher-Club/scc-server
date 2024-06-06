package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.stdlib.di.annotation.Component
import mu.KotlinLogging
import java.nio.file.Files
import java.nio.file.Path

@Component
class DeleteLocalThumbnailImagesUseCase {
    private val logger = KotlinLogging.logger {}

    fun handle(rootPath: Path) {
        Files.walk(rootPath).use {
            it.forEach { path ->
                try {
                    Files.delete(path)
                } catch (t: Throwable) {
                    logger.error(t) { "Failed to delete file: $path" }
                }
            }
        }
    }
}
