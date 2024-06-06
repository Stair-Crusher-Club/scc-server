package club.staircrusher.domain_event

import club.staircrusher.stdlib.domain.event.DomainEvent
import java.nio.file.Path

data class AccessibilityThumbnailGeneratedEvent(
    val path: Path
) : DomainEvent
