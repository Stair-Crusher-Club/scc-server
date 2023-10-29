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
import club.staircrusher.place.application.port.`in`.PlaceService
import club.staircrusher.place.application.port.`in`.toBuildingDTO
import club.staircrusher.place.application.port.`in`.toPlaceDTO
import club.staircrusher.place.domain.model.Building
import club.staircrusher.place.domain.model.Place
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.stdlib.domain.event.DomainEventPublisher
import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager
import kotlinx.coroutines.runBlocking

@Component
class DeleteAccessibilityUseCase(
    private val transactionManager: TransactionManager,
    private val placeAccessibilityRepository: PlaceAccessibilityRepository,
    private val placeAccessibilityCommentRepository: PlaceAccessibilityCommentRepository,
    private val buildingAccessibilityRepository: BuildingAccessibilityRepository,
    private val buildingAccessibilityCommentRepository: BuildingAccessibilityCommentRepository,
    private val placeService: PlaceService,
    private val domainEventPublisher: DomainEventPublisher,
) {
    fun handle(
        userId: String,
        placeAccessibilityId: String,
    ) : Unit = transactionManager.doInTransaction(TransactionIsolationLevel.SERIALIZABLE) {
        val placeAccessibility = placeAccessibilityRepository.findById(placeAccessibilityId)
        if (!placeAccessibility.isDeletable(userId)) {
            throw SccDomainException("삭제 가능한 장소 정보가 아닙니다.")
        }

        val place = placeService.findPlace(placeAccessibility.placeId)!!
        deletePlaceAccessibility(userId, placeAccessibility, place)
        deletePlaceAccessibilityComments(place)

        val building = place.building
        if (placeAccessibilityRepository.findByBuildingId(building.id).isEmpty()) {
            val buildingAccessibility = buildingAccessibilityRepository.findByBuildingId(building.id) ?: return@doInTransaction
            deleteBuildingAccessibility(buildingAccessibility, building)
            deleteBuildingAccessibilityComments(building)
        }
    }

    private fun deletePlaceAccessibility(
        userId: String,
        placeAccessibility: PlaceAccessibility,
        place: Place,
    ) {
        placeAccessibilityRepository.remove(placeAccessibility.id)
        transactionManager.doAfterCommit {
            runBlocking {
                domainEventPublisher.publishEvent(PlaceAccessibilityDeletedEvent(
                    id = placeAccessibility.id,
                    accessibilityRegisterer = AccessibilityRegistererDTO(
                        id = userId,
                    ),
                    place = place.toPlaceDTO(),
                ))
            }
        }
    }

    private fun deletePlaceAccessibilityComments(place: Place) {
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

    private fun deleteBuildingAccessibility(
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

    private fun deleteBuildingAccessibilityComments(building: Building) {
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
