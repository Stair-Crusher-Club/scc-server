package club.staircrusher.domain_event

import club.staircrusher.domain_event.dto.AccessibilityRegistererDTO
import club.staircrusher.domain_event.dto.BuildingDTO
import club.staircrusher.stdlib.domain.event.DomainEvent


data class BuildingAccessibilityDeletedEvent(
    val id: String,
    val accessibilityRegisterer: AccessibilityRegistererDTO,
    val building: BuildingDTO,
) : DomainEvent
