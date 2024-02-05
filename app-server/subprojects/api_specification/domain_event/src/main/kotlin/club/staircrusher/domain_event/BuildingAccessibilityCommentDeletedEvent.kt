package club.staircrusher.domain_event

import club.staircrusher.domain_event.dto.AccessibilityCommentRegistererDTO
import club.staircrusher.domain_event.dto.BuildingDTO
import club.staircrusher.stdlib.domain.event.DomainEvent


data class BuildingAccessibilityCommentDeletedEvent(
    val id: String,
    val commentRegisterer: AccessibilityCommentRegistererDTO,
    val building: BuildingDTO,
) : DomainEvent
