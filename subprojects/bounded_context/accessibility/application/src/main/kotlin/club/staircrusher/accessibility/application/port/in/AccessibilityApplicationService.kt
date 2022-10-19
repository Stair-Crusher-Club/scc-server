package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.UserInfo
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityCommentRepository
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityRepository
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityUpvoteRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityCommentRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.accessibility.application.port.out.web.PlaceService
import club.staircrusher.accessibility.application.toDomainModel
import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.BuildingAccessibilityComment
import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.accessibility.domain.model.PlaceAccessibilityComment
import club.staircrusher.accessibility.domain.model.StairInfo
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.application.port.`in`.UserApplicationService
import java.time.Clock

@Component
class AccessibilityApplicationService(
    private val transactionManager: TransactionManager,
    private val placeService: PlaceService,
    private val placeAccessibilityRepository: PlaceAccessibilityRepository,
    private val placeAccessibilityCommentRepository: PlaceAccessibilityCommentRepository,
    private val buildingAccessibilityRepository: BuildingAccessibilityRepository,
    private val buildingAccessibilityCommentRepository: BuildingAccessibilityCommentRepository,
    private val buildingAccessibilityUpvoteRepository: BuildingAccessibilityUpvoteRepository,
    // FIXME: do not use other BC's application service directly
    private val userApplicationService: UserApplicationService,
    private val clock: Clock,
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
        val place = placeService.findPlace(placeId) ?: error("Cannot find place with $placeId")
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
                isUpvoted = buildingAccessibilityUpvoteRepository.findExistingUpvote(
                    userId,
                    it,
                ) != null,
                totalUpvoteCount = buildingAccessibilityUpvoteRepository.countUpvotes(buildingAccessibility.id),
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
        placeAccessibilityRepository.findByPlaceId(placeId)
    }

    fun getBuildingAccessibility(placeId: String): BuildingAccessibility? = transactionManager.doInTransaction {
        val place = placeService.findPlace(placeId) ?: return@doInTransaction null
        buildingAccessibilityRepository.findByBuildingId(place.buildingId)
    }

    data class RegisterAccessibilityResult(
        val placeAccessibility: PlaceAccessibility,
        val placeAccessibilityComment: PlaceAccessibilityComment?,
        val buildingAccessibility: BuildingAccessibility?,
        val buildingAccessibilityComment: BuildingAccessibilityComment?,
        val userInfo: UserInfo?,
    )

    @Suppress("LongMethod")
    fun register(
        createPlaceAccessibilityParams: PlaceAccessibilityRepository.CreateParams,
        createPlaceAccessibilityCommentParams: PlaceAccessibilityCommentRepository.CreateParams?,
        createBuildingAccessibilityParams: BuildingAccessibilityRepository.CreateParams?,
        createBuildingAccessibilityCommentParams: BuildingAccessibilityCommentRepository.CreateParams?,
    ): RegisterAccessibilityResult = transactionManager.doInTransaction(TransactionIsolationLevel.REPEATABLE_READ) {
        if (placeAccessibilityRepository.findByPlaceId(createPlaceAccessibilityParams.placeId) != null) {
            throw SccDomainException("이미 접근성 정보가 등록된 장소입니다.")
        }
        val result = placeAccessibilityRepository.save(
            PlaceAccessibility(
                id = EntityIdGenerator.generateRandom(),
                placeId = createPlaceAccessibilityParams.placeId,
                isFirstFloor = createPlaceAccessibilityParams.isFirstFloor,
                stairInfo = createPlaceAccessibilityParams.stairInfo,
                hasSlope = createPlaceAccessibilityParams.hasSlope,
                imageUrls = createPlaceAccessibilityParams.imageUrls,
                userId = createPlaceAccessibilityParams.userId,
                createdAt = clock.instant(),
            )
        )
        val placeAccessibilityComment = createPlaceAccessibilityCommentParams?.let {
            val normalizedComment = it.comment.trim()
            if (normalizedComment.isBlank()) {
                throw SccDomainException("한 글자 이상의 의견을 제출해주세요.")
            }
            placeAccessibilityCommentRepository.save(
                PlaceAccessibilityComment(
                    id = EntityIdGenerator.generateRandom(),
                    placeId = it.placeId,
                    userId = it.userId,
                    comment = normalizedComment,
                    createdAt = clock.instant(),
                )
            )
        }
        val buildingAccessibility = createBuildingAccessibilityParams?.let {
            if (buildingAccessibilityRepository.findByBuildingId(it.buildingId) != null) {
                throw SccDomainException("이미 접근성 정보가 등록된 건물입니다.")
            }
            if (
                it.hasElevator && it.elevatorStairInfo == StairInfo.UNDEFINED ||
                !it.hasElevator && it.elevatorStairInfo != StairInfo.UNDEFINED
            ) {
                throw SccDomainException("엘레베이터 유무 정보와 엘레베이터까지의 계단 개수 정보가 맞지 않습니다.") // TODO: 테스트 추가
            }
            buildingAccessibilityRepository.save(
                BuildingAccessibility(
                    id = EntityIdGenerator.generateRandom(),
                    buildingId = it.buildingId,
                    entranceStairInfo = it.entranceStairInfo,
                    hasSlope = it.hasSlope,
                    hasElevator = it.hasElevator,
                    elevatorStairInfo = it.elevatorStairInfo,
                    imageUrls = it.imageUrls,
                    userId = it.userId,
                    createdAt = clock.instant(),
                )
            )
        }
        val buildingAccessibilityComment = createBuildingAccessibilityCommentParams?.let {
            val normalizedComment = it.comment.trim()
            if (normalizedComment.isBlank()) {
                throw SccDomainException("한 글자 이상의 의견을 제출해주세요.")
            }
            buildingAccessibilityCommentRepository.save(
                BuildingAccessibilityComment(
                    id = EntityIdGenerator.generateRandom(),
                    buildingId = it.buildingId,
                    userId = it.userId,
                    comment = normalizedComment,
                    createdAt = clock.instant(),
                )
            )
        }
        val userInfo = createPlaceAccessibilityParams.userId?.let { userApplicationService.getUser(it) }?.toDomainModel()

        RegisterAccessibilityResult(
            placeAccessibility = result,
            placeAccessibilityComment = placeAccessibilityComment,
            buildingAccessibility = buildingAccessibility,
            buildingAccessibilityComment = buildingAccessibilityComment,
            userInfo = userInfo,
        )
    }

    fun registerBuildingAccessibilityComment(
        params: BuildingAccessibilityCommentRepository.CreateParams,
    ): WithUserInfo<BuildingAccessibilityComment> = transactionManager.doInTransaction(TransactionIsolationLevel.REPEATABLE_READ) {
        val normalizedComment = params.comment.trim()
        if (normalizedComment.isBlank()) {
            throw SccDomainException("한 글자 이상의 의견을 제출해주세요.")
        }
        val comment = buildingAccessibilityCommentRepository.save(
            BuildingAccessibilityComment(
                id = EntityIdGenerator.generateRandom(),
                buildingId = params.buildingId,
                userId = params.userId,
                comment = normalizedComment,
                createdAt = clock.instant(),
            )
        )
        WithUserInfo(
            value = comment,
            userInfo = params.userId?.let { userApplicationService.getUser(it) }?.toDomainModel(),
        )
    }

    fun registerPlaceAccessibilityComment(
        params: PlaceAccessibilityCommentRepository.CreateParams,
    ): WithUserInfo<PlaceAccessibilityComment> = transactionManager.doInTransaction(TransactionIsolationLevel.REPEATABLE_READ) {
        val normalizedComment = params.comment.trim()
        if (normalizedComment.isBlank()) {
            throw SccDomainException("한 글자 이상의 의견을 제출해주세요.")
        }
        val comment = placeAccessibilityCommentRepository.save(
            PlaceAccessibilityComment(
                id = EntityIdGenerator.generateRandom(),
                placeId = params.placeId,
                userId = params.userId,
                comment = normalizedComment,
                createdAt = clock.instant(),
            )
        )
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
        val buildingAccessibilities = buildingAccessibilityRepository.findByPlaceIds(placeAccessibilities.map { it.placeId })
        return Pair(placeAccessibilities, buildingAccessibilities)
    }
}
