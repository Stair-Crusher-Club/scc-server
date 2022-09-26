package club.staircrusher.accessibility.application

import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.BuildingAccessibilityComment
import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.accessibility.domain.model.PlaceAccessibilityComment
import club.staircrusher.accessibility.domain.repository.BuildingAccessibilityCommentRepository
import club.staircrusher.accessibility.domain.repository.BuildingAccessibilityRepository
import club.staircrusher.accessibility.domain.repository.PlaceAccessibilityCommentRepository
import club.staircrusher.accessibility.domain.repository.PlaceAccessibilityRepository
import club.staircrusher.accessibility.domain.service.BuildingAccessibilityCommentService
import club.staircrusher.accessibility.domain.service.BuildingAccessibilityService
import club.staircrusher.accessibility.domain.service.PlaceAccessibilityCommentService
import club.staircrusher.accessibility.domain.service.PlaceAccessibilityService
import club.staircrusher.accessibility.domain.service.PlaceService
import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager
import org.springframework.stereotype.Component

@Component
class AccessibilityApplicationService(
    private val transactionManager: TransactionManager,
    private val placeService: PlaceService,
    private val placeAccessibilityRepository: PlaceAccessibilityRepository,
    private val placeAccessibilityCommentRepository: PlaceAccessibilityCommentRepository,
    private val buildingAccessibilityRepository: BuildingAccessibilityRepository,
    private val buildingAccessibilityCommentRepository: BuildingAccessibilityCommentRepository,
    private val placeAccessibilityService: PlaceAccessibilityService,
    private val placeAccessibilityCommentService: PlaceAccessibilityCommentService,
    private val buildingAccessibilityService: BuildingAccessibilityService,
    private val buildingAccessibilityCommentService: BuildingAccessibilityCommentService,
) {
    data class GetAccessibilityResult(
        val buildingAccessibility: BuildingAccessibility?,
        val buildingAccessibilityComments: List<BuildingAccessibilityComment>,
        val placeAccessibility: PlaceAccessibility?,
        val placeAccessibilityComments: List<PlaceAccessibilityComment>,
        val hasOtherPlacesToRegisterInSameBuilding: Boolean,
    )

    fun getAccessibility(placeId: String): GetAccessibilityResult = transactionManager.doInTransaction {
        val place = placeService.findPlace(placeId) ?: throw IllegalArgumentException("Cannot find place with $placeId")
        GetAccessibilityResult(
            buildingAccessibility = buildingAccessibilityRepository.findByBuildingId(place.buildingId),
            buildingAccessibilityComments = buildingAccessibilityCommentRepository.findByBuildingId(place.buildingId),
            placeAccessibility = placeAccessibilityRepository.findByPlaceId(placeId),
            placeAccessibilityComments = placeAccessibilityCommentRepository.findByPlaceId(placeId),
            hasOtherPlacesToRegisterInSameBuilding = placeAccessibilityRepository.hasAccessibilityNotRegisteredPlaceInBuilding(place.buildingId)
        )
    }

    data class RegisterAccessibilityResult(
        val placeAccessibility: PlaceAccessibility,
        val placeAccessibilityComment: PlaceAccessibilityComment?,
        val buildingAccessibility: BuildingAccessibility?,
        val buildingAccessibilityComment: BuildingAccessibilityComment?,
    )

    fun register(
        createPlaceAccessibilityParams: PlaceAccessibilityService.CreateParams,
        createPlaceAccessibilityCommentParams: PlaceAccessibilityCommentService.CreateParams?,
        createBuildingAccessibilityParams: BuildingAccessibilityService.CreateParams?,
        createBuildingAccessibilityCommentParams: BuildingAccessibilityCommentService.CreateParams?,
    ): RegisterAccessibilityResult = transactionManager.doInTransaction(TransactionIsolationLevel.SERIALIZABLE) {
        val placeAccessibility = placeAccessibilityService.create(createPlaceAccessibilityParams)
        val placeAccessibilityComment = createPlaceAccessibilityCommentParams?.let { placeAccessibilityCommentService.create(it) }
        val buildingAccessibility = createBuildingAccessibilityParams?.let { buildingAccessibilityService.create(it) }
        val buildingAccessibilityComment = createBuildingAccessibilityCommentParams?.let { buildingAccessibilityCommentService.create(it) }

        RegisterAccessibilityResult(
            placeAccessibility = placeAccessibility,
            placeAccessibilityComment = placeAccessibilityComment,
            buildingAccessibility = buildingAccessibility,
            buildingAccessibilityComment = buildingAccessibilityComment,
        )
    }

    fun registerBuildingAccessibilityComment(
        params: BuildingAccessibilityCommentService.CreateParams,
    ): BuildingAccessibilityComment = transactionManager.doInTransaction(TransactionIsolationLevel.SERIALIZABLE) {
        buildingAccessibilityCommentService.create(params)
    }

    fun registerPlaceAccessibilityComment(
        params: PlaceAccessibilityCommentService.CreateParams,
    ): PlaceAccessibilityComment = transactionManager.doInTransaction(TransactionIsolationLevel.SERIALIZABLE) {
        placeAccessibilityCommentService.create(params)
    }

    fun filterAccessibilityExistingPlaceIds(placeIds: List<String>): List<String> {
        return placeAccessibilityRepository.findByPlaceIds(placeIds).map { it.placeId }
    }
}
