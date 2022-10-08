package club.staircrusher.accessibility.application

import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.BuildingAccessibilityComment
import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.accessibility.domain.model.PlaceAccessibilityComment
import club.staircrusher.accessibility.domain.repository.BuildingAccessibilityCommentRepository
import club.staircrusher.accessibility.domain.repository.BuildingAccessibilityRepository
import club.staircrusher.accessibility.domain.repository.BuildingAccessibilityUpvoteRepository
import club.staircrusher.accessibility.domain.repository.PlaceAccessibilityCommentRepository
import club.staircrusher.accessibility.domain.repository.PlaceAccessibilityRepository
import club.staircrusher.accessibility.domain.service.BuildingAccessibilityCommentService
import club.staircrusher.accessibility.domain.service.BuildingAccessibilityService
import club.staircrusher.accessibility.domain.service.BuildingAccessibilityUpvoteService
import club.staircrusher.accessibility.domain.service.PlaceAccessibilityCommentService
import club.staircrusher.accessibility.domain.service.PlaceAccessibilityService
import club.staircrusher.accessibility.domain.service.PlaceService
import club.staircrusher.stdlib.auth.AuthUser
import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.application.user.UserApplicationService
import club.staircrusher.stdlib.di.annotation.Component

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
    private val buildingAccessibilityUpvoteService: BuildingAccessibilityUpvoteService,
    private val buildingAccessibilityUpvoteRepository: BuildingAccessibilityUpvoteRepository,
    private val userApplicationService: UserApplicationService,
) {
    data class GetAccessibilityResult(
        val buildingAccessibility: WithUserInfo<BuildingAccessibility>?,
        val buildingAccessibilityUpvoteInfo: BuildingAccessibilityUpvoteInfo?,
        val buildingAccessibilityComments: List<WithUserInfo<BuildingAccessibilityComment>>,
        val placeAccessibility: WithUserInfo<PlaceAccessibility>?,
        val placeAccessibilityComments: List<WithUserInfo<PlaceAccessibilityComment>>,
        val hasOtherPlacesToRegisterInSameBuilding: Boolean,
    ) {
         data class BuildingAccessibilityUpvoteInfo(
             val isUpvoted: Boolean,
             val totalUpvoteCount: Int,
         )
    }

    data class WithUserInfo<T>(
        val value: T,
        val userInfo: UserInfo?
    )

    fun getAccessibility(placeId: String, userId: String): GetAccessibilityResult = transactionManager.doInTransaction {
        val place = placeService.findPlace(placeId) ?: throw IllegalArgumentException("Cannot find place with $placeId")
        val buildingAccessibility = buildingAccessibilityRepository.findByBuildingId(place.buildingId)
        val buildingAccessibilityComments = buildingAccessibilityCommentRepository.findByBuildingId(place.buildingId)
        val placeAccessibility = placeAccessibilityRepository.findByPlaceId(placeId)
        val placeAccessibilityComments = placeAccessibilityCommentRepository.findByPlaceId(placeId)
        val userInfoById = userApplicationService.getUsers(
            listOfNotNull(buildingAccessibility?.userId)
                    + buildingAccessibilityComments.mapNotNull { it.userId }
                    + listOfNotNull(placeAccessibility?.userId)
                    + placeAccessibilityComments.mapNotNull { it.userId }
        ).map { it.toDomainModel() }.associateBy { it.userId }
        val buildingAccessibilityUpvoteInfo = buildingAccessibility?.let {
            GetAccessibilityResult.BuildingAccessibilityUpvoteInfo(
                isUpvoted = buildingAccessibilityUpvoteService.isUpvoted(userId, it),
                totalUpvoteCount = buildingAccessibilityUpvoteRepository.getTotalUpvoteCount(place.buildingId),
            )
        }

        GetAccessibilityResult(
            buildingAccessibility = buildingAccessibility?.let { WithUserInfo(it, userInfoById[it.userId]) },
            buildingAccessibilityUpvoteInfo = buildingAccessibilityUpvoteInfo,
            buildingAccessibilityComments = buildingAccessibilityComments.map {
                WithUserInfo(
                    value = it,
                    userInfo = userInfoById[it.userId],
                )
            },
            placeAccessibility = placeAccessibility?.let { WithUserInfo(it, userInfoById[it.userId]) },
            placeAccessibilityComments = placeAccessibilityComments.map {
                WithUserInfo(
                    value = it,
                    userInfo = userInfoById[it.userId],
                )
            },
            hasOtherPlacesToRegisterInSameBuilding = placeAccessibilityRepository.hasAccessibilityNotRegisteredPlaceInBuilding(place.buildingId)
        )
    }

    fun getPlaceAccessibility(placeId: String): PlaceAccessibility? = transactionManager.doInTransaction {
        val place = placeService.findPlace(placeId) ?: throw IllegalArgumentException("Cannot find place with $placeId")
        placeAccessibilityRepository.findByPlaceId(placeId)
    }

    fun getBuildingAccessibility(placeId: String): BuildingAccessibility? = transactionManager.doInTransaction {
        val place = placeService.findPlace(placeId) ?: throw IllegalArgumentException("Cannot find place with $placeId")
        buildingAccessibilityRepository.findByBuildingId(place.buildingId)
    }

    data class RegisterAccessibilityResult(
        val placeAccessibility: PlaceAccessibility,
        val placeAccessibilityComment: PlaceAccessibilityComment?,
        val buildingAccessibility: BuildingAccessibility?,
        val buildingAccessibilityComment: BuildingAccessibilityComment?,
        val userInfo: UserInfo?,
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
        val userInfo = createPlaceAccessibilityParams.userId?.let { userApplicationService.getUser(it) }?.toDomainModel()

        RegisterAccessibilityResult(
            placeAccessibility = placeAccessibility,
            placeAccessibilityComment = placeAccessibilityComment,
            buildingAccessibility = buildingAccessibility,
            buildingAccessibilityComment = buildingAccessibilityComment,
            userInfo = userInfo,
        )
    }

    fun registerBuildingAccessibilityComment(
        params: BuildingAccessibilityCommentService.CreateParams,
    ): WithUserInfo<BuildingAccessibilityComment> = transactionManager.doInTransaction(TransactionIsolationLevel.SERIALIZABLE) {
        val comment = buildingAccessibilityCommentService.create(params)
        WithUserInfo(
            value = comment,
            userInfo = params.userId?.let { userApplicationService.getUser(it) }?.toDomainModel(),
        )
    }

    fun registerPlaceAccessibilityComment(
        params: PlaceAccessibilityCommentService.CreateParams,
    ): WithUserInfo<PlaceAccessibilityComment> = transactionManager.doInTransaction(TransactionIsolationLevel.SERIALIZABLE) {
        val comment = placeAccessibilityCommentService.create(params)
        WithUserInfo(
            value = comment,
            userInfo = params.userId?.let { userApplicationService.getUser(it) }?.toDomainModel(),
        )
    }

    fun filterAccessibilityExistingPlaceIds(placeIds: List<String>): List<String> = transactionManager.doInTransaction {
        placeAccessibilityRepository.findByPlaceIds(placeIds).map { it.placeId }
    }

    fun findByUserId(userId: String): Pair<List<PlaceAccessibility>, List<BuildingAccessibility>> {
        val placeAccessibilities = placeAccessibilityRepository.findByUserId(userId)
        val buildingAccessibilities = buildingAccessibilityRepository.findByPlaceIds(placeAccessibilities.map { it.id })
        return Pair(placeAccessibilities, buildingAccessibilities)
    }
}
