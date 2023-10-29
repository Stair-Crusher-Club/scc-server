package club.staircrusher.domain_event

import club.staircrusher.domain_event.dto.AccessibilityCommentRegistererDTO
import club.staircrusher.domain_event.dto.PlaceDTO
import club.staircrusher.stdlib.domain.event.DomainEvent


data class PlaceAccessibilityCommentDeletedEvent(
    val id: String,
    val commentRegisterer: AccessibilityCommentRegistererDTO,
    val place: PlaceDTO,
) : DomainEvent
