package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityCommentRepository
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityCommentRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.domain_event.BuildingAccessibilityCommentDeletedEvent
import club.staircrusher.domain_event.BuildingAccessibilityDeletedEvent
import club.staircrusher.domain_event.PlaceAccessibilityCommentDeletedEvent
import club.staircrusher.domain_event.PlaceAccessibilityDeletedEvent
import club.staircrusher.domain_event.dto.AccessibilityCommentRegistererDTO
import club.staircrusher.domain_event.dto.AccessibilityRegistererDTO
import club.staircrusher.place.application.port.`in`.toBuildingDTO
import club.staircrusher.place.application.port.`in`.toPlaceDTO
import club.staircrusher.place.domain.model.Building
import club.staircrusher.place.domain.model.Place
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.event.DomainEventPublisher
import club.staircrusher.stdlib.persistence.TransactionManager
import kotlinx.coroutines.runBlocking

@Component
class DeleteAccessibilityAplService(
    private val transactionManager: TransactionManager,
    private val placeAccessibilityRepository: PlaceAccessibilityRepository,
    private val placeAccessibilityCommentRepository: PlaceAccessibilityCommentRepository,
    private val buildingAccessibilityRepository: BuildingAccessibilityRepository,
    private val buildingAccessibilityCommentRepository: BuildingAccessibilityCommentRepository,
    private val domainEventPublisher: DomainEventPublisher,
) {
    internal fun deletePlaceAccessibility(
        placeAccessibility: PlaceAccessibility,
        place: Place,
    ) {
        doDeletePlaceAccessibility(placeAccessibility, place)
        doDeletePlaceAccessibilityComments(place)
    }

    internal fun deleteBuildingAccessibility(
        buildingAccessibility: BuildingAccessibility,
        building: Building,
    ) {
        doDeleteBuildingAccessibility(buildingAccessibility, building)
        doDeleteBuildingAccessibilityComments(building)
    }

    private fun doDeletePlaceAccessibility(
        placeAccessibility: PlaceAccessibility,
        place: Place,
    ) {
        placeAccessibilityRepository.remove(placeAccessibility.id)
        transactionManager.doAfterCommit {
            runBlocking {
                domainEventPublisher.publishEvent(PlaceAccessibilityDeletedEvent(
                    id = placeAccessibility.id,
                    accessibilityRegisterer = AccessibilityRegistererDTO(
                        id = placeAccessibility.userId,
                    ),
                    place = place.toPlaceDTO(),
                ))
            }
        }
    }

    private fun doDeletePlaceAccessibilityComments(place: Place) {
        val placeAccessibilityComments = placeAccessibilityCommentRepository.findByPlaceId(place.id)
        placeAccessibilityCommentRepository.removeByPlaceId(place.id)
        transactionManager.doAfterCommit {
            runBlocking {
                placeAccessibilityComments.forEach { comment ->
                    domainEventPublisher.publishEvent(PlaceAccessibilityCommentDeletedEvent(
                        id = comment.id,
                        commentRegisterer = AccessibilityCommentRegistererDTO(
                            id = comment.userId,
                        ),
                        place = place.toPlaceDTO(),
                    ))
                }
            }
        }
    }

    private fun doDeleteBuildingAccessibility(
        buildingAccessibility: BuildingAccessibility,
        building: Building,
    ) {
        buildingAccessibilityRepository.remove(buildingAccessibility.id)
        transactionManager.doAfterCommit {
            runBlocking {
                domainEventPublisher.publishEvent(BuildingAccessibilityDeletedEvent(
                    id = buildingAccessibility.id,
                    accessibilityRegisterer = AccessibilityRegistererDTO(
                        id = buildingAccessibility.userId,
                    ),
                    building = building.toBuildingDTO(),
                ))
            }
        }
    }

    private fun doDeleteBuildingAccessibilityComments(building: Building) {
        val buildingAccessibilityComments = buildingAccessibilityCommentRepository.findByBuildingId(building.id)
        buildingAccessibilityCommentRepository.removeByBuildingId(building.id)
        transactionManager.doAfterCommit {
            runBlocking {
                buildingAccessibilityComments.forEach { comment ->
                    domainEventPublisher.publishEvent(BuildingAccessibilityCommentDeletedEvent(
                        id = comment.id,
                        commentRegisterer = AccessibilityCommentRegistererDTO(
                            id = comment.userId,
                        ),
                        building = building.toBuildingDTO(),
                    ))
                }
            }
        }
    }
}
