package club.staircrusher.place.application.port.`in`.accessibility

import club.staircrusher.challenge.application.port.`in`.ChallengeService
import club.staircrusher.place.application.port.`in`.accessibility.result.GetAccessibilityResult
import club.staircrusher.place.application.port.`in`.accessibility.result.RegisterBuildingAccessibilityResult
import club.staircrusher.place.application.port.`in`.accessibility.result.RegisterPlaceAccessibilityResult
import club.staircrusher.place.application.port.`in`.accessibility.result.WithUserInfo
import club.staircrusher.place.application.port.`in`.place.BuildingService
import club.staircrusher.place.application.port.`in`.place.PlaceApplicationService
import club.staircrusher.place.application.port.out.accessibility.persistence.BuildingAccessibilityCommentRepository
import club.staircrusher.place.application.port.out.accessibility.persistence.BuildingAccessibilityRepository
import club.staircrusher.place.application.port.out.accessibility.persistence.BuildingAccessibilityUpvoteRepository
import club.staircrusher.place.application.port.out.accessibility.persistence.AccessibilityImageRepository
import club.staircrusher.place.application.port.out.accessibility.persistence.PlaceAccessibilityCommentRepository
import club.staircrusher.place.application.port.out.accessibility.persistence.PlaceAccessibilityRepository
import club.staircrusher.place.application.result.AccessibilityRegisterer
import club.staircrusher.place.application.result.toDomainModel
import club.staircrusher.place.domain.model.accessibility.AccessibilityImageOld
import club.staircrusher.place.domain.model.accessibility.BuildingAccessibility
import club.staircrusher.place.domain.model.accessibility.BuildingAccessibilityComment
import club.staircrusher.place.domain.model.accessibility.AccessibilityImage
import club.staircrusher.place.domain.model.accessibility.PlaceAccessibility
import club.staircrusher.place.domain.model.accessibility.PlaceAccessibilityComment
import club.staircrusher.place.domain.model.accessibility.StairInfo
import club.staircrusher.place.domain.model.place.Building
import club.staircrusher.place.domain.model.place.Place
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.persistence.TimestampCursor
import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.application.port.`in`.UserApplicationService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.Instant

@Suppress("TooManyFunctions")
@Component
class AccessibilityApplicationService(
    private val transactionManager: TransactionManager,
    private val placeApplicationService: PlaceApplicationService,
    private val buildingService: BuildingService,
    private val placeAccessibilityRepository: PlaceAccessibilityRepository,
    private val placeAccessibilityCommentRepository: PlaceAccessibilityCommentRepository,
    private val accessibilityImageRepository: AccessibilityImageRepository,
    private val buildingAccessibilityRepository: BuildingAccessibilityRepository,
    private val buildingAccessibilityCommentRepository: BuildingAccessibilityCommentRepository,
    private val buildingAccessibilityUpvoteRepository: BuildingAccessibilityUpvoteRepository,
    // FIXME: do not use other BC's application service directly
    private val userApplicationService: UserApplicationService,
    private val challengeService: ChallengeService,
    private val accessibilityAllowedRegionService: AccessibilityAllowedRegionService,
) {

    fun isAccessibilityRegistrable(place: Place): Boolean {
        return !place.isClosed && isAccessibilityRegistrable(place.building)
    }

    fun isAccessibilityRegistrable(building: Building): Boolean {
        val addressStr = building.address.toString()
        return (addressStr.startsWith("서울") || addressStr.startsWith("경기 성남시")) ||
            accessibilityAllowedRegionService.isAccessibilityAllowed(building.location)
    }

    fun getAccessibility(placeId: String, userId: String?): GetAccessibilityResult {
        return transactionManager.doInTransaction {
            doGetAccessibility(placeId, userId)
        }
    }

    internal fun doGetAccessibility(placeId: String, userId: String?): GetAccessibilityResult {
        val place = placeApplicationService.findPlace(placeId) ?: error("Cannot find place with $placeId")
        val buildingAccessibility =
            buildingAccessibilityRepository.findFirstByBuildingIdAndDeletedAtIsNull(place.building.id)
        val buildingAccessibilityComments = buildingAccessibilityCommentRepository.findByBuildingId(place.building.id)
        val buildingAccessibilityChallengeCrusherGroup =
            buildingAccessibility?.id?.let { challengeService.getBuildingAccessibilityCrusherGroup(it) }
        val placeAccessibility = placeAccessibilityRepository.findFirstByPlaceIdAndDeletedAtIsNull(placeId)
        val placeAccessibilityComments = placeAccessibilityCommentRepository.findByPlaceId(placeId)
        val placeAccessibilityChallengeCrusherGroup =
            placeAccessibility?.id?.let { challengeService.getPlaceAccessibilityCrusherGroup(it) }
        val userInfoById = userApplicationService.getProfilesByUserIds(
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
                        buildingAccessibility.id,
                    )
                } != null,
                totalUpvoteCount = buildingAccessibilityUpvoteRepository.countByBuildingAccessibilityId(
                    buildingAccessibility.id
                ),
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
            buildingAccessibilityChallengeCrusherGroup = buildingAccessibilityChallengeCrusherGroup,
            placeAccessibility = placeAccessibility?.let { WithUserInfo(it, userInfoById[it.userId]) },
            placeAccessibilityComments = placeAccessibilityComments.map {
                WithUserInfo(
                    value = it,
                    accessibilityRegisterer = userInfoById[it.userId],
                )
            },
            placeAccessibilityChallengeCrusherGroup = placeAccessibilityChallengeCrusherGroup,
            hasOtherPlacesToRegisterInSameBuilding = hasOtherPlacesToRegisterInSameBuilding(place.building),
            isFavoritePlace = userId?.let { placeApplicationService.isFavoritePlace(placeId, it) } ?: false,
            totalFavoriteCount = placeApplicationService.getTotalFavoriteCount(placeId),
        )
    }

    private fun hasOtherPlacesToRegisterInSameBuilding(building: Building): Boolean {
        val placesInBuilding = placeApplicationService.findByBuildingId(building.id)
        val placeAccessibilityExistingPlaceIds =
            placeAccessibilityRepository.findByPlaceIdInAndDeletedAtIsNull(placesInBuilding.map { it.id })
                .map { it.placeId }
        return placesInBuilding.any { it.id !in placeAccessibilityExistingPlaceIds }
    }

    fun listPlaceAndBuildingAccessibility(places: List<Place>): List<Pair<PlaceAccessibility?, BuildingAccessibility?>> {
        if (places.isEmpty()) {
            return emptyList()
        }
        val placeIds = places.map { it.id }
        // 현재 place 당 pa, ba 는 정책상 1개 이므로 단순 associateBy 해준다.
        val pas = placeAccessibilityRepository.findByPlaceIdInAndDeletedAtIsNull(placeIds).associateBy { it.placeId }
        val buildingIds = places.map { it.building.id }.toSet()
        val bas = buildingAccessibilityRepository.findByBuildingIdInAndDeletedAtIsNull(buildingIds)
            .associateBy { it.buildingId }
        return places.map {
            pas[it.id] to bas[it.building.id]
        }
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
    )

    @Deprecated("PlaceAccessibilty, BuildingAccessibility 함수가 분리")
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
            )
        }
        return registerResult
    }

    @Suppress("ThrowsCount")
    internal fun doRegisterBuildingAccessibility(
        createBuildingAccessibilityParams: BuildingAccessibilityRepository.CreateParams,
        createBuildingAccessibilityCommentParams: BuildingAccessibilityCommentRepository.CreateParams?,
    ): RegisterBuildingAccessibilityResult {
        if (createBuildingAccessibilityParams.isValid().not()) {
            throw SccDomainException(
                "잘못된 접근성 정보입니다. 필수 입력을 빠트렸거나 조건을 다시 한 번 확인해주세요.",
                SccDomainException.ErrorCode.INVALID_ARGUMENTS
            )
        }
        val buildingId = createBuildingAccessibilityParams.buildingId
        if (buildingAccessibilityRepository.findFirstByBuildingIdAndDeletedAtIsNull(buildingId) != null) {
            throw SccDomainException("이미 접근성 정보가 등록된 건물입니다.")
        }
        val building = buildingService.getById(buildingId)!!
        if (!isAccessibilityRegistrable(building)) {
            throw SccDomainException("접근성 정보를 등록할 수 없는 건물입니다.")
        }
        val buildingAccessibility = createBuildingAccessibilityParams.let {
            if (
                (it.hasElevator && it.elevatorStairInfo == StairInfo.UNDEFINED) ||
                (it.hasElevator.not() && it.elevatorStairInfo != StairInfo.UNDEFINED)
            ) {
                throw SccDomainException("엘레베이터 유무 정보와 엘레베이터까지의 계단 개수 정보가 맞지 않습니다.")
            }
            val entranceImages =
                it.entranceImageUrls.map { url -> AccessibilityImageOld(imageUrl = url, thumbnailUrl = null) }
            val elevatorImages =
                it.elevatorImageUrls.map { url -> AccessibilityImageOld(imageUrl = url, thumbnailUrl = null) }

            val buildingAccessibility = buildingAccessibilityRepository.save(
                BuildingAccessibility(
                    id = EntityIdGenerator.generateRandom(),
                    buildingId = it.buildingId,
                    entranceStairInfo = it.entranceStairInfo,
                    entranceStairHeightLevel = it.entranceStairHeightLevel,
                    oldEntranceImageUrls = it.entranceImageUrls,
                    oldEntranceImages = entranceImages,
                    hasSlope = it.hasSlope,
                    hasElevator = it.hasElevator,
                    entranceDoorTypes = it.entranceDoorTypes,
                    elevatorStairInfo = it.elevatorStairInfo,
                    elevatorStairHeightLevel = it.elevatorStairHeightLevel,
                    oldElevatorImageUrls = it.elevatorImageUrls,
                    oldElevatorImages = elevatorImages,
                    userId = it.userId,
                    createdAt = SccClock.instant(),
                )
            )
                .apply {
                    val savedImages = accessibilityImageRepository.saveAll(
                        it.entranceImageUrls.mapIndexed { index, img ->
                            AccessibilityImage(
                                id = EntityIdGenerator.generateRandom(),
                                accessibilityId = id,
                                accessibilityType = AccessibilityImage.AccessibilityType.Building,
                                imageType = AccessibilityImage.ImageType.Entrance,
                                originalImageUrl = img,
                                displayOrder = index,
                            )
                        } + it.elevatorImageUrls.mapIndexed { index, img ->
                            AccessibilityImage(
                                id = EntityIdGenerator.generateRandom(),
                                accessibilityId = id,
                                accessibilityType = AccessibilityImage.AccessibilityType.Building,
                                imageType = AccessibilityImage.ImageType.Elevator,
                                originalImageUrl = img,
                                displayOrder = index,
                            )
                        }
                    )
                    this.entranceImages =
                        savedImages.filter { it.imageType == AccessibilityImage.ImageType.Entrance }.toMutableList()
                    this.elevatorImages =
                        savedImages.filter { it.imageType == AccessibilityImage.ImageType.Elevator }.toMutableList()
                }
            buildingAccessibility
        }
        val buildingAccessibilityComment = createBuildingAccessibilityCommentParams?.let {
            doRegisterBuildingAccessibilityComment(it)
        }
        val userInfo =
            createBuildingAccessibilityParams.userId?.let { userApplicationService.getProfileByUserIdOrNull(it) }
                ?.toDomainModel()
        return RegisterBuildingAccessibilityResult(
            building = building,
            buildingAccessibility = buildingAccessibility,
            buildingAccessibilityComment = buildingAccessibilityComment,
            accessibilityRegisterer = userInfo,
        )
    }

    @Suppress("ThrowsCount")
    internal fun doRegisterPlaceAccessibility(
        createPlaceAccessibilityParams: PlaceAccessibilityRepository.CreateParams,
        createPlaceAccessibilityCommentParams: PlaceAccessibilityCommentRepository.CreateParams?,
    ): RegisterPlaceAccessibilityResult {
        if (placeAccessibilityRepository.findFirstByPlaceIdAndDeletedAtIsNull(createPlaceAccessibilityParams.placeId) != null) {
            throw SccDomainException("이미 접근성 정보가 등록된 장소입니다.")
        }
        val place = placeApplicationService.findPlace(createPlaceAccessibilityParams.placeId)!!
        if (!isAccessibilityRegistrable(place)) {
            throw SccDomainException("접근성 정보를 등록할 수 없는 장소입니다.")
        }

        val result = placeAccessibilityRepository.save(
            PlaceAccessibility(
                id = EntityIdGenerator.generateRandom(),
                placeId = createPlaceAccessibilityParams.placeId,
                floors = createPlaceAccessibilityParams.floors,
                isFirstFloor = createPlaceAccessibilityParams.isFirstFloor
                    ?: createPlaceAccessibilityParams.floors?.let { it.size == 1 && it.first() == 1 } ?: false,
                isStairOnlyOption = createPlaceAccessibilityParams.isStairOnlyOption,
                stairInfo = createPlaceAccessibilityParams.stairInfo,
                stairHeightLevel = createPlaceAccessibilityParams.stairHeightLevel,
                hasSlope = createPlaceAccessibilityParams.hasSlope,
                entranceDoorTypes = createPlaceAccessibilityParams.entranceDoorTypes,
                oldImageUrls = createPlaceAccessibilityParams.imageUrls,
                oldImages = createPlaceAccessibilityParams.imageUrls.map {
                    AccessibilityImageOld(
                        imageUrl = it,
                        thumbnailUrl = null
                    )
                },
                userId = createPlaceAccessibilityParams.userId,
                createdAt = SccClock.instant(),
            )
        ).also {
            it.images = accessibilityImageRepository.saveAll(
                createPlaceAccessibilityParams.imageUrls.mapIndexed { index, img ->
                    AccessibilityImage(
                        id = EntityIdGenerator.generateRandom(),
                        accessibilityId = it.id,
                        accessibilityType = AccessibilityImage.AccessibilityType.Place,
                        originalImageUrl = img,
                        displayOrder = index,
                    )
                }
            ).toMutableList()
        }
        val placeAccessibilityComment = createPlaceAccessibilityCommentParams?.let {
            doRegisterPlaceAccessibilityComment(it)
        }

        val userInfo =
            createPlaceAccessibilityParams.userId?.let { userApplicationService.getProfileByUserIdOrNull(it) }
                ?.toDomainModel()

        return RegisterPlaceAccessibilityResult(
            place = place,
            placeAccessibility = result,
            placeAccessibilityComment = placeAccessibilityComment,
            accessibilityRegisterer = userInfo,
            registrationOrder = placeAccessibilityRepository.countByDeletedAtIsNull(),
        )
    }

    fun registerBuildingAccessibilityComment(
        params: BuildingAccessibilityCommentRepository.CreateParams,
    ): WithUserInfo<BuildingAccessibilityComment> =
        transactionManager.doInTransaction(TransactionIsolationLevel.REPEATABLE_READ) {
            val comment = doRegisterBuildingAccessibilityComment(params)
            WithUserInfo(
                value = comment,
                accessibilityRegisterer = params.userId?.let { userApplicationService.getProfileByUserIdOrNull(it) }
                    ?.toDomainModel(),
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
                createdAt = SccClock.instant(),
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
                accessibilityRegisterer = params.userId?.let { userApplicationService.getProfileByUserIdOrNull(it) }
                    ?.toDomainModel(),
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
                createdAt = SccClock.instant(),
            )
        )
    }

    fun filterAccessibilityExistingPlaceIds(placeIds: List<String>): List<String> {
        if (placeIds.isEmpty()) return emptyList()

        return transactionManager.doInTransaction {
            placeAccessibilityRepository.findByPlaceIdInAndDeletedAtIsNull(placeIds).map { it.placeId }
        }
    }

    fun findCursoredByUserId(
        userId: String,
        pageable: Pageable,
        cursor: TimestampCursor
    ): Pair<Page<PlaceAccessibility>, List<BuildingAccessibility>> {
        val placeAccessibilityPage =
            placeAccessibilityRepository.findCursoredByUserId(userId, pageable, cursor.timestamp, cursor.id)
        if (placeAccessibilityPage.content.isEmpty()) return Pair(Page.empty(), emptyList())

        val buildingIds = placeApplicationService.findAllByIds(placeAccessibilityPage.content.map { it.placeId })
            .map { it.building.id }
        val buildingAccessibilities = buildingAccessibilityRepository.findByBuildingIdInAndDeletedAtIsNull(buildingIds)

        return Pair(placeAccessibilityPage, buildingAccessibilities)
    }

    fun countByUserId(userId: String): Int {
        return placeAccessibilityRepository.countByUserIdAndDeletedAtIsNull(userId)
    }

    fun countByUserIdAndCreatedAtBetween(userId: String, from: Instant, to: Instant): Int =
        placeAccessibilityRepository.countByUserIdAndCreatedAtBetweenAndDeletedAtIsNull(
            userId,
            from,
            to
        ) + buildingAccessibilityRepository.countByUserIdAndCreatedAtBetweenAndDeletedAtIsNull(
            userId,
            from,
            to
        )

    fun findByUserIdAndCreatedAtBetween(
        userId: String,
        from: Instant,
        to: Instant
    ): Pair<List<PlaceAccessibility>, List<BuildingAccessibility>> {
        val placeAccessibilities =
            placeAccessibilityRepository.findByUserIdAndCreatedAtBetweenAndDeletedAtIsNull(userId, from, to)
        val buildingAccessibilities =
            buildingAccessibilityRepository.findByUserIdAndCreatedAtBetweenAndDeletedAtIsNull(userId, from, to)
        return Pair(placeAccessibilities, buildingAccessibilities)
    }

    fun countNearby(currentLocation: Location, limit: Int): Int {
        val maxLimit = maxOf(limit, MAX_DISTANCE_METERS)
        val placeIds = placeApplicationService.searchPlaceIdsInCircle(currentLocation, maxLimit)

        return placeAccessibilityRepository.countByPlaceIdIn(placeIds)
    }

    companion object {
        private const val MAX_DISTANCE_METERS = 20_000 // 20km
    }
}
