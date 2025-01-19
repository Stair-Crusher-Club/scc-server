package club.staircrusher.testing.spring_it

import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityCommentRepository
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityRepository
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityUpvoteRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityCommentRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.accessibility.domain.model.AccessibilityImage
import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.BuildingAccessibilityComment
import club.staircrusher.accessibility.domain.model.BuildingAccessibilityUpvote
import club.staircrusher.accessibility.domain.model.EntranceDoorType
import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.accessibility.domain.model.PlaceAccessibilityComment
import club.staircrusher.accessibility.domain.model.StairHeightLevel
import club.staircrusher.accessibility.domain.model.StairInfo
import club.staircrusher.challenge.application.port.out.persistence.ChallengeContributionRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeParticipationRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeRepository
import club.staircrusher.challenge.domain.model.Challenge
import club.staircrusher.challenge.domain.model.ChallengeCondition
import club.staircrusher.challenge.domain.model.ChallengeContribution
import club.staircrusher.challenge.domain.model.ChallengeCrusherGroup
import club.staircrusher.challenge.domain.model.ChallengeParticipation
import club.staircrusher.external_accessibility.application.port.out.persistence.ExternalAccessibilityRepository
import club.staircrusher.external_accessibility.domain.model.ExternalAccessibility
import club.staircrusher.external_accessibility.domain.model.ToiletAccessibilityDetails
import club.staircrusher.place.application.port.out.persistence.BuildingRepository
import club.staircrusher.place.application.port.out.persistence.PlaceFavoriteRepository
import club.staircrusher.place.application.port.out.persistence.PlaceRepository
import club.staircrusher.place.domain.model.Building
import club.staircrusher.place.domain.model.BuildingAddress
import club.staircrusher.place.domain.model.Place
import club.staircrusher.place.domain.model.PlaceFavorite
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.external_accessibility.ExternalAccessibilityCategory
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.geography.eupMyeonDongById
import club.staircrusher.stdlib.geography.siGunGuById
import club.staircrusher.stdlib.testing.SccRandom
import club.staircrusher.user.application.port.out.persistence.IdentifiedUserRepository
import club.staircrusher.user.domain.model.IdentifiedUser
import club.staircrusher.user.domain.model.UserMobilityTool
import club.staircrusher.user.domain.service.PasswordEncryptor
import org.springframework.beans.factory.annotation.Autowired
import java.time.Clock
import java.time.Instant

@Suppress("MagicNumber", "TooManyFunctions")
@Component
class ITDataGenerator {
    @Autowired
    private lateinit var clock: Clock

    @Autowired
    private lateinit var passwordEncryptor: PasswordEncryptor

    @Autowired
    private lateinit var identifiedUserRepository: IdentifiedUserRepository

    @Autowired
    private lateinit var placeRepository: PlaceRepository

    @Autowired
    private lateinit var placeFavoriteRepository: PlaceFavoriteRepository

    @Autowired
    private lateinit var buildingRepository: BuildingRepository

    @Autowired
    private lateinit var placeAccessibilityRepository: PlaceAccessibilityRepository

    @Autowired
    private lateinit var buildingAccessibilityRepository: BuildingAccessibilityRepository

    @Autowired
    private lateinit var placeAccessibilityCommentRepository: PlaceAccessibilityCommentRepository

    @Autowired
    private lateinit var buildingAccessibilityCommentRepository: BuildingAccessibilityCommentRepository

    @Autowired
    private lateinit var buildingAccessibilityUpvoteRepository: BuildingAccessibilityUpvoteRepository

    @Autowired
    private lateinit var challengeRepository: ChallengeRepository

    @Autowired
    private lateinit var challengeParticipationRepository: ChallengeParticipationRepository

    @Autowired
    private lateinit var challengeContributionRepository: ChallengeContributionRepository

    @Autowired
    private lateinit var externalAccessibilityRepository: ExternalAccessibilityRepository

    fun createUser(
        nickname: String = SccRandom.string(12),
        password: String = "password",
        email: String = "${SccRandom.string(12)}@staircrusher.club",
        instagramId: String? = null,
        mobilityTools: List<UserMobilityTool> = emptyList(),
    ): IdentifiedUser {
        return identifiedUserRepository.save(
            IdentifiedUser(
                id = EntityIdGenerator.generateRandom(),
                nickname = nickname,
                encryptedPassword = passwordEncryptor.encrypt(password.trim()),
                instagramId = instagramId?.trim()?.takeIf { it.isNotEmpty() },
                email = email,
                mobilityTools = mobilityTools.toMutableList(),
            )
        )
    }

    fun createPlace(
        placeName: String = "장소장소",
        building: Building,
        isClosed: Boolean = false,
        isNotAccessible: Boolean = false,
    ): Place {
        return placeRepository.save(
            Place.of(
                id = EntityIdGenerator.generateRandom(),
                name = placeName,
                location = building.location,
                building = building,
                siGunGuId = building.siGunGuId,
                eupMyeonDongId = building.eupMyeonDongId,
                isClosed = isClosed,
                isNotAccessible = isNotAccessible,
            )
        )
    }

    fun createBuilding(
        location: Location = Location(127.5, 37.5),
        eupMyeonDongId: String = eupMyeonDongById.keys.first(),
        siGunGuId: String = siGunGuById.keys.first(),
    ): Building {
        return buildingRepository.save(
            Building(
                id = EntityIdGenerator.generateRandom(),
                name = "건물건물",
                location = location,
                address = BuildingAddress(
                    siDo = "서울특별시",
                    siGunGu = "성동구",
                    eupMyeonDong = "성수동",
                    li = "",
                    roadName = "왕십리로",
                    mainBuildingNumber = "83",
                    subBuildingNumber = "21",
                ),
                siGunGuId = siGunGuId,
                eupMyeonDongId = eupMyeonDongId,
            )
        )
    }

    fun createExternalAccessibility(
        name: String = "공공화장실",
        location: Location = Location(127.5, 37.5),
        address: String = "임의주소",
        category: ExternalAccessibilityCategory = ExternalAccessibilityCategory.TOILET,
    ): ExternalAccessibility {
        return externalAccessibilityRepository.save(
            ExternalAccessibility(
                id = EntityIdGenerator.generateRandom(),
                name = name,
                location = location,
                address = address,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                category = category,
                toiletDetails = ToiletAccessibilityDetails(gender = "남자화장실", availableDesc = "asdf"),
            )
        )
    }

    fun createBuildingAndPlace(
        placeName: String = "장소장소",
        location: Location = Location(127.5, 37.5),
        building: Building? = null,
        eupMyeonDongId: String = eupMyeonDongById.keys.first(),
        siGunGuId: String = siGunGuById.keys.first(),
        buildingAddress: BuildingAddress = BuildingAddress(
            siDo = "서울특별시",
            siGunGu = "성동구",
            eupMyeonDong = "성수동",
            li = "",
            roadName = "왕십리로",
            mainBuildingNumber = "83",
            subBuildingNumber = "21",
        ),
        placeIsClosed: Boolean = false,
        placeIsNotAccessible: Boolean = false,
    ): Place {
        val buildingToUse = building ?: buildingRepository.save(
            Building(
                id = EntityIdGenerator.generateRandom(),
                name = "건물건물",
                location = location,
                address = buildingAddress,
                siGunGuId = siGunGuId,
                eupMyeonDongId = eupMyeonDongId,
            )
        )
        return createPlace(
            placeName = placeName,
            building = buildingToUse,
            isClosed = placeIsClosed,
            isNotAccessible = placeIsNotAccessible,
        )
    }

    fun createChallenge(
        name: String = "",
        isPublic: Boolean = true,
        invitationCode: String? = null,
        passcode: String? = null,
        isComplete: Boolean = false,
        startsAt: Instant = clock.instant(),
        endsAt: Instant? = null,
        goal: Int = 1000,
        milestones: List<Int> = listOf(),
        conditions: List<ChallengeCondition> = listOf(),
        description: String = "",
        crusherGroup: ChallengeCrusherGroup? = null
    ): Challenge {
        return challengeRepository.save(
            Challenge(
                id = EntityIdGenerator.generateRandom(),
                name = name,
                isPublic = isPublic,
                invitationCode = invitationCode,
                passcode = passcode,
                isComplete = isComplete,
                startsAt = startsAt,
                endsAt = endsAt,
                goal = goal,
                milestones = milestones,
                conditions = conditions,
                createdAt = clock.instant(),
                updatedAt = clock.instant(),
                description = description,
                crusherGroup = crusherGroup
            )
        )
    }

    fun createPlaceFavorite(
        userId: String,
        placeId: String,
        at: Instant = clock.instant()
    ): PlaceFavorite {
        return placeFavoriteRepository.save(
            PlaceFavorite(
                id = EntityIdGenerator.generateRandom(),
                userId = userId,
                placeId = placeId,
                createdAt = at,
                updatedAt = at,
                deletedAt = null,
            )
        )
    }

    fun participateChallenge(
        user: IdentifiedUser,
        challenge: Challenge,
        participateAt: Instant
    ): ChallengeParticipation {
        return challengeParticipationRepository.save(
            ChallengeParticipation(
                id = EntityIdGenerator.generateRandom(),
                challengeId = challenge.id,
                userId = user.id,
                createdAt = participateAt
            )
        )
    }

    fun contributeToChallenge(
        user: IdentifiedUser,
        challenge: Challenge,
        placeAccessibility: PlaceAccessibility? = null,
        placeAccessibilityComment: PlaceAccessibilityComment? = null,
        buildingAccessibility: BuildingAccessibility? = null,
        buildingAccessibilityComment: BuildingAccessibilityComment? = null,
        contributeAt: Instant
    ): ChallengeContribution {
        val contribution = challengeContributionRepository.save(
            ChallengeContribution(
                id = EntityIdGenerator.generateRandom(),
                userId = user.id,
                challengeId = challenge.id,
                placeAccessibilityId = placeAccessibility?.id,
                placeAccessibilityCommentId = placeAccessibilityComment?.id,
                buildingAccessibilityId = buildingAccessibility?.id,
                buildingAccessibilityCommentId = buildingAccessibilityComment?.id,
                createdAt = contributeAt,
                updatedAt = contributeAt
            )
        )
        challengeRepository.save(
            challenge.also {
                it.isComplete = challenge.goal <= challengeContributionRepository.countByChallengeId(
                    challengeId = challenge.id
                )
            }
        )
        return contribution
    }

    fun registerPlaceAccessibility(
        place: Place,
        floors: List<Int> = listOf(1),
        isStairOnlyOption: Boolean = false,
        stairInfo: StairInfo = StairInfo.ONE,
        stairHeightLevel: StairHeightLevel = StairHeightLevel.HALF_THUMB,
        hasSlope: Boolean = true,
        entranceDoorTypes: List<EntranceDoorType> = listOf(EntranceDoorType.Sliding, EntranceDoorType.Automatic),
        imageUrls: List<String> = emptyList(),
        images: List<AccessibilityImage> = emptyList(),
        user: IdentifiedUser? = null,
        at: Instant = clock.instant(),
    ): PlaceAccessibility {
        return placeAccessibilityRepository.save(
            PlaceAccessibility(
                id = EntityIdGenerator.generateRandom(),
                placeId = place.id,
                floors = floors,
                isFirstFloor = true,
                isStairOnlyOption = isStairOnlyOption,
                stairInfo = stairInfo,
                stairHeightLevel = stairHeightLevel,
                hasSlope = hasSlope,
                entranceDoorTypes = entranceDoorTypes,
                imageUrls = imageUrls,
                images = images,
                userId = user?.id,
                createdAt = at,
            ),
        )
    }

    fun registerBuildingAccessibilityIfNotExists(
        building: Building,
        entranceStairInfo: StairInfo = StairInfo.NONE,
        entranceStairHeightLevel: StairHeightLevel = StairHeightLevel.THUMB,
        entranceImageUrls: List<String> = emptyList(),
        entranceImages: List<AccessibilityImage> = emptyList(),
        entranceDoorTypes: List<EntranceDoorType> = listOf(EntranceDoorType.Sliding, EntranceDoorType.Automatic),
        hasSlope: Boolean = true,
        hasElevator: Boolean = true,
        elevatorStairHeightLevel: StairHeightLevel = StairHeightLevel.HALF_THUMB,
        elevatorImageUrls: List<String> = emptyList(),
        elevatorImages: List<AccessibilityImage> = emptyList(),
        user: IdentifiedUser? = null,
        at: Instant = clock.instant(),
    ): BuildingAccessibility {
        return buildingAccessibilityRepository.findFirstByBuildingIdAndDeletedAtIsNull(building.id) ?: buildingAccessibilityRepository.save(
            BuildingAccessibility(
                id = EntityIdGenerator.generateRandom(),
                buildingId = building.id,
                entranceStairInfo = entranceStairInfo,
                entranceStairHeightLevel = entranceStairHeightLevel,
                entranceImageUrls = entranceImageUrls,
                entranceImages = entranceImages,
                hasSlope = hasSlope,
                hasElevator = hasElevator,
                entranceDoorTypes = entranceDoorTypes,
                elevatorStairInfo = StairInfo.NONE,
                elevatorStairHeightLevel = elevatorStairHeightLevel,
                elevatorImageUrls = elevatorImageUrls,
                elevatorImages = elevatorImages,
                userId = user?.id,
                createdAt = at,
            ),
        )
    }

    fun registerBuildingAndPlaceAccessibility(
        place: Place,
        user: IdentifiedUser? = null,
        imageUrls: List<String> = emptyList(),
        images: List<AccessibilityImage> = emptyList(),
        at: Instant = clock.instant(),
    ): Pair<PlaceAccessibility, BuildingAccessibility> {
        return Pair(
            registerPlaceAccessibility(place = place, user = user, imageUrls = imageUrls, images = images, at = at),
            registerBuildingAccessibilityIfNotExists(
                place.building,
                user = user,
                entranceImageUrls = imageUrls,
                entranceImages = images,
                elevatorImageUrls = imageUrls,
                elevatorImages = images,
                at = at
            ),
        )
    }

    fun registerBuildingAccessibilityComment(
        building: Building,
        comment: String,
        user: IdentifiedUser? = null
    ): BuildingAccessibilityComment {
        return buildingAccessibilityCommentRepository.save(
            BuildingAccessibilityComment(
                id = EntityIdGenerator.generateRandom(),
                buildingId = building.id,
                userId = user?.id,
                comment = comment,
                createdAt = clock.instant(),
            ),
        )
    }

    fun registerPlaceAccessibilityComment(
        place: Place,
        comment: String,
        user: IdentifiedUser? = null
    ): PlaceAccessibilityComment {
        return placeAccessibilityCommentRepository.save(
            PlaceAccessibilityComment(
                id = EntityIdGenerator.generateRandom(),
                placeId = place.id,
                userId = user?.id,
                comment = comment,
                createdAt = clock.instant(),
            ),
        )
    }

    fun giveBuildingAccessibilityUpvote(buildingAccessibility: BuildingAccessibility, user: IdentifiedUser = createUser()) {
        buildingAccessibilityUpvoteRepository.save(
            BuildingAccessibilityUpvote(
                id = EntityIdGenerator.generateRandom(),
                userId = user.id,
                buildingAccessibilityId = buildingAccessibility.id,
                createdAt = clock.instant(),
            ),
        )
    }
}
