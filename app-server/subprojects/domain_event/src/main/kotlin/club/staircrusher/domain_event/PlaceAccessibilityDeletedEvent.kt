package club.staircrusher.domain_event

import club.staircrusher.domain_event.dto.AccessibilityRegistererDTO
import club.staircrusher.domain_event.dto.PlaceDTO
import club.staircrusher.stdlib.domain.event.DomainEvent


data class PlaceAccessibilityDeletedEvent(
    val id: String,
    val accessibilityRegisterer: AccessibilityRegistererDTO,
    val place: PlaceDTO,
) : DomainEvent
