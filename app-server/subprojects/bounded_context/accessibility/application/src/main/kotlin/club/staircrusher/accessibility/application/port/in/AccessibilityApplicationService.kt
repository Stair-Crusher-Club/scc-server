package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.AccessibilityRegisterer
import club.staircrusher.accessibility.application.port.`in`.result.GetAccessibilityResult
import club.staircrusher.accessibility.application.port.`in`.result.RegisterBuildingAccessibilityResult
import club.staircrusher.accessibility.application.port.`in`.result.RegisterPlaceAccessibilityResult
import club.staircrusher.accessibility.application.port.`in`.result.WithUserInfo
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityCommentRepository
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityRepository
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityUpvoteRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityCommentRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.accessibility.application.toDomainModel
import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.BuildingAccessibilityComment
import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.accessibility.domain.model.PlaceAccessibilityComment
import club.staircrusher.accessibility.domain.model.StairInfo
import club.staircrusher.place.application.port.`in`.BuildingService
import club.staircrusher.place.application.port.`in`.PlaceService
import club.staircrusher.place.domain.model.Building
import club.staircrusher.place.domain.model.Place
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.application.port.`in`.UserApplicationService
import java.time.Clock

@Suppress("TooManyFunctions")
@Component
class AccessibilityApplicationService(
    private val transactionManager: TransactionManager,
    private val placeService: PlaceService,
    private val buildingService: BuildingService,
    private val placeAccessibilityRepository: PlaceAccessibilityRepository,
    private val placeAccessibilityCommentRepository: PlaceAccessibilityCommentRepository,
    private val buildingAccessibilityRepository: BuildingAccessibilityRepository,
    private val buildingAccessibilityCommentRepository: BuildingAccessibilityCommentRepository,
    private val buildingAccessibilityUpvoteRepository: BuildingAccessibilityUpvoteRepository,
    // FIXME: do not use other BC's application service directly
    private val userApplicationService: UserApplicationService,
    private val accessibilityAllowedRegionService: AccessibilityAllowedRegionService,
    private val clock: Clock,
) {
    private val Building.isAccessibilityRegistrable: Boolean
        get() {
            val addressStr = address.toString()
            return (addressStr.startsWith("서울") || addressStr.startsWith("경기 성남시")) ||
                accessibilityAllowedRegionService.isAccessibilityAllowed(location)
        }

    fun isAccessibilityRegistrable(placeId: String): Boolean {
        val place = placeService.findPlace(placeId) ?: error("Cannot find place with $placeId")
        return place.building.isAccessibilityRegistrable
    }

    fun getAccessibility(placeId: String, userId: String?): GetAccessibilityResult =
        transactionManager.doInTransaction {
            doGetAccessibility(placeId, userId)
        }

    internal fun doGetAccessibility(placeId: String, userId: String?): GetAccessibilityResult {
        val place = placeService.findPlace(placeId) ?: error("Cannot find place with $placeId")
        val buildingAccessibility = buildingAccessibilityRepository.findByBuildingId(place.building.id)
        val buildingAccessibilityComments = buildingAccessibilityCommentRepository.findByBuildingId(place.building.id)
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
                isUpvoted = userId?.let {
                    buildingAccessibilityUpvoteRepository.findExistingUpvote(
                        userId,
                        buildingAccessibility,
                    )
                } != null,
                totalUpvoteCount = buildingAccessibilityUpvoteRepository.countUpvotes(buildingAccessibility.id),
            )
        }

        return GetAccessibilityResult(
            buildingAccessibility = buildingAccessibility?.let { WithUserInfo(it, userInfoById[it.userId]) },
            buildingAccessibilityUpvoteInfo = buildingAccessibilityUpvoteInfo,
            buildingAccessibilityComments = buildingAccessibilityComments.map {
                WithUserInfo(
                    value = it,
                    accessibilityRegisterer = userInfoById[it.userId],
                )
            },
            placeAccessibility = placeAccessibility?.let { WithUserInfo(it, userInfoById[it.userId]) },
            placeAccessibilityComments = placeAccessibilityComments.map {
                WithUserInfo(
                    value = it,
                    accessibilityRegisterer = userInfoById[it.userId],
                )
            },
            hasOtherPlacesToRegisterInSameBuilding = placeAccessibilityRepository.hasAccessibilityNotRegisteredPlaceInBuilding(
                place.building.id
            ),
            isLastPlaceAccessibilityInBuilding = placeAccessibility?.isLastPlaceAccessibilityInBuilding(place.building.id)
                ?: false,
        )
    }

    private fun PlaceAccessibility.isLastPlaceAccessibilityInBuilding(buildingId: String): Boolean {
        return placeAccessibilityRepository.findByBuildingId(buildingId).let {
            it.size == 1 && it[0].id == this.id
        }
    }

    fun getPlaceAccessibility(placeId: String): PlaceAccessibility? = transactionManager.doInTransaction {
        placeAccessibilityRepository.findByPlaceId(placeId)
    }

    fun getBuildingAccessibility(placeId: String): BuildingAccessibility? = transactionManager.doInTransaction {
        val place = placeService.findPlace(placeId) ?: return@doInTransaction null
        buildingAccessibilityRepository.findByBuildingId(place.building.id)
    }

    data class RegisterAccessibilityResult(
        val place: Place?,
        val placeAccessibility: PlaceAccessibility,
        val placeAccessibilityComment: PlaceAccessibilityComment?,
        val building: Building?,
        val buildingAccessibility: BuildingAccessibility?,
        val buildingAccessibilityComment: BuildingAccessibilityComment?,
        val accessibilityRegisterer: AccessibilityRegisterer?,
        val registrationOrder: Int, // n번째 정복자를 표현하기 위한 값.
        val isLastPlaceAccessibilityInBuilding: Boolean,
    )

    @Suppress("LongMethod", "ComplexMethod")
    fun register(
        createPlaceAccessibilityParams: PlaceAccessibilityRepository.CreateParams,
        createPlaceAccessibilityCommentParams: PlaceAccessibilityCommentRepository.CreateParams?,
        createBuildingAccessibilityParams: BuildingAccessibilityRepository.CreateParams?,
        createBuildingAccessibilityCommentParams: BuildingAccessibilityCommentRepository.CreateParams?,
    ): RegisterAccessibilityResult {
        val registerResult = transactionManager.doInTransaction(TransactionIsolationLevel.REPEATABLE_READ) {
            val registerPlaceAccessibilityResult =
                doRegisterPlaceAccessibility(createPlaceAccessibilityParams, createPlaceAccessibilityCommentParams)
            val registerBuildingAccessibilityResult = createBuildingAccessibilityParams?.let {
                doRegisterBuildingAccessibility(
                    it,
                    createBuildingAccessibilityCommentParams
                )
            }

            return@doInTransaction RegisterAccessibilityResult(
                place = registerPlaceAccessibilityResult.place,
                placeAccessibility = registerPlaceAccessibilityResult.placeAccessibility,
                placeAccessibilityComment = registerPlaceAccessibilityResult.placeAccessibilityComment,
                building = registerBuildingAccessibilityResult?.building,
                buildingAccessibility = registerBuildingAccessibilityResult?.buildingAccessibility,
                buildingAccessibilityComment = registerBuildingAccessibilityResult?.buildingAccessibilityComment,
                accessibilityRegisterer = registerPlaceAccessibilityResult.accessibilityRegisterer,
                registrationOrder = registerPlaceAccessibilityResult.registrationOrder,
                isLastPlaceAccessibilityInBuilding = registerPlaceAccessibilityResult.isLastPlaceAccessibilityInBuilding,
            )
        }
        return registerResult
    }

    @Suppress("ThrowsCount")
    internal fun doRegisterBuildingAccessibility(
        createBuildingAccessibilityParams: BuildingAccessibilityRepository.CreateParams,
        createBuildingAccessibilityCommentParams: BuildingAccessibilityCommentRepository.CreateParams?,
    ): RegisterBuildingAccessibilityResult {
        val buildingId = createBuildingAccessibilityParams.buildingId
        if (buildingAccessibilityRepository.findByBuildingId(buildingId) != null) {
            throw SccDomainException("이미 접근성 정보가 등록된 건물입니다.")
        }
        val building = buildingService.getById(buildingId)!!
        if (!building.isAccessibilityRegistrable) {
            throw SccDomainException("접근성 정보를 등록할 수 없는 장소입니다.")
        }
        val buildingAccessibility = createBuildingAccessibilityParams.let {
            if (
                it.hasElevator && it.elevatorStairInfo == StairInfo.UNDEFINED ||
                !it.hasElevator && it.elevatorStairInfo != StairInfo.UNDEFINED
            ) {
                throw SccDomainException("엘레베이터 유무 정보와 엘레베이터까지의 계단 개수 정보가 맞지 않습니다.")
            }
            buildingAccessibilityRepository.save(
                BuildingAccessibility(
                    id = EntityIdGenerator.generateRandom(),
                    buildingId = it.buildingId,
                    entranceStairInfo = it.entranceStairInfo,
                    entranceImageUrls = it.entranceImageUrls,
                    hasSlope = it.hasSlope,
                    hasElevator = it.hasElevator,
                    elevatorStairInfo = it.elevatorStairInfo,
                    elevatorImageUrls = it.elevatorImageUrls,
                    userId = it.userId,
                    createdAt = clock.instant(),
                )
            )
        }
        val buildingAccessibilityComment = createBuildingAccessibilityCommentParams?.let {
            doRegisterBuildingAccessibilityComment(it)
        }
        val userInfo =
            createBuildingAccessibilityParams.userId?.let { userApplicationService.getUser(it) }?.toDomainModel()
        return RegisterBuildingAccessibilityResult(
            building = building,
            buildingAccessibility = buildingAccessibility,
            buildingAccessibilityComment = buildingAccessibilityComment,
            accessibilityRegisterer = userInfo,
        )
    }

    internal fun doRegisterPlaceAccessibility(
        createPlaceAccessibilityParams: PlaceAccessibilityRepository.CreateParams,
        createPlaceAccessibilityCommentParams: PlaceAccessibilityCommentRepository.CreateParams?,
    ): RegisterPlaceAccessibilityResult {
        if (placeAccessibilityRepository.findByPlaceId(createPlaceAccessibilityParams.placeId) != null) {
            throw SccDomainException("이미 접근성 정보가 등록된 장소입니다.")
        }
        val place = placeService.findPlace(createPlaceAccessibilityParams.placeId)!!
        val building = place.building
        if (!building.isAccessibilityRegistrable) {
            throw SccDomainException("접근성 정보를 등록할 수 없는 장소입니다.")
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
            doRegisterPlaceAccessibilityComment(it)
        }

        val userInfo =
            createPlaceAccessibilityParams.userId?.let { userApplicationService.getUser(it) }?.toDomainModel()
        val buildingId = building.id

        return RegisterPlaceAccessibilityResult(
            place = place,
            placeAccessibility = result,
            placeAccessibilityComment = placeAccessibilityComment,
            accessibilityRegisterer = userInfo,
            registrationOrder = placeAccessibilityRepository.countAll(),
            isLastPlaceAccessibilityInBuilding = result.isLastPlaceAccessibilityInBuilding(buildingId) ?: false,
        )
    }

    fun registerBuildingAccessibilityComment(
        params: BuildingAccessibilityCommentRepository.CreateParams,
    ): WithUserInfo<BuildingAccessibilityComment> =
        transactionManager.doInTransaction(TransactionIsolationLevel.REPEATABLE_READ) {
            val comment = doRegisterBuildingAccessibilityComment(params)
            WithUserInfo(
                value = comment,
                accessibilityRegisterer = params.userId?.let { userApplicationService.getUser(it) }?.toDomainModel(),
            )
        }

    private fun doRegisterBuildingAccessibilityComment(
        params: BuildingAccessibilityCommentRepository.CreateParams,
    ): BuildingAccessibilityComment {
        val normalizedComment = params.comment.trim()
        if (normalizedComment.isBlank()) {
            throw SccDomainException("한 글자 이상의 의견을 제출해주세요.")
        }
        return buildingAccessibilityCommentRepository.save(
            BuildingAccessibilityComment(
                id = EntityIdGenerator.generateRandom(),
                buildingId = params.buildingId,
                userId = params.userId,
                comment = normalizedComment,
                createdAt = clock.instant(),
            )
        )
    }

    fun registerPlaceAccessibilityComment(
        params: PlaceAccessibilityCommentRepository.CreateParams,
    ): WithUserInfo<PlaceAccessibilityComment> =
        transactionManager.doInTransaction(TransactionIsolationLevel.REPEATABLE_READ) {
            val comment = doRegisterPlaceAccessibilityComment(params)
            WithUserInfo(
                value = comment,
                accessibilityRegisterer = params.userId?.let { userApplicationService.getUser(it) }?.toDomainModel(),
            )
        }

    private fun doRegisterPlaceAccessibilityComment(
        params: PlaceAccessibilityCommentRepository.CreateParams,
    ): PlaceAccessibilityComment {
        val normalizedComment = params.comment.trim()
        if (normalizedComment.isBlank()) {
            throw SccDomainException("한 글자 이상의 의견을 제출해주세요.")
        }
        return placeAccessibilityCommentRepository.save(
            PlaceAccessibilityComment(
                id = EntityIdGenerator.generateRandom(),
                placeId = params.placeId,
                userId = params.userId,
                comment = normalizedComment,
                createdAt = clock.instant(),
            )
        )
    }

    fun filterAccessibilityExistingPlaceIds(placeIds: List<String>): List<String> = transactionManager.doInTransaction {
        placeAccessibilityRepository.findByPlaceIds(placeIds).map { it.placeId }
    }

    fun findByUserId(userId: String): Pair<List<PlaceAccessibility>, List<BuildingAccessibility>> {
        val placeAccessibilities = placeAccessibilityRepository.findByUserId(userId)
        val buildingAccessibilities =
            buildingAccessibilityRepository.findByPlaceIds(placeAccessibilities.map { it.placeId })
        return Pair(placeAccessibilities, buildingAccessibilities)
    }
}
