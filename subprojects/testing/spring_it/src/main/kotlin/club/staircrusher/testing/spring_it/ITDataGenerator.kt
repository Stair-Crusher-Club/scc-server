package club.staircrusher.testing.spring_it

import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityCommentRepository
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityRepository
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityUpvoteRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityCommentRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.BuildingAccessibilityComment
import club.staircrusher.accessibility.domain.model.BuildingAccessibilityUpvote
import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.accessibility.domain.model.PlaceAccessibilityComment
import club.staircrusher.accessibility.domain.model.StairInfo
import club.staircrusher.place.application.port.out.persistence.BuildingRepository
import club.staircrusher.place.application.port.out.persistence.PlaceRepository
import club.staircrusher.place.domain.model.Building
import club.staircrusher.place.domain.model.BuildingAddress
import club.staircrusher.place.domain.model.Place
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.geography.eupMyeonDongById
import club.staircrusher.stdlib.geography.siGunGuById
import club.staircrusher.user.application.port.out.persistence.UserRepository
import club.staircrusher.user.domain.model.User
import club.staircrusher.user.domain.service.PasswordEncryptor
import org.springframework.beans.factory.annotation.Autowired
import java.time.Clock
import java.util.Base64
import kotlin.random.Random

@Suppress("MagicNumber", "TooManyFunctions")
@Component
class ITDataGenerator {
    @Autowired
    private lateinit var clock: Clock

    @Autowired
    private lateinit var passwordEncryptor: PasswordEncryptor

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var placeRepository: PlaceRepository

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


    fun createUser(
        nickname: String = generateRandomString(12),
        password: String = "password",
        instagramId: String? = null
    ): User {
        return userRepository.save(
            User(
                id = EntityIdGenerator.generateRandom(),
                nickname = nickname,
                encryptedPassword = passwordEncryptor.encrypt(password.trim()),
                instagramId = instagramId?.trim()?.takeIf { it.isNotEmpty() },
                createdAt = clock.instant(),
            )
        )
    }

    fun createPlace(
        placeName: String = "장소장소",
        building: Building,
    ): Place {
        return placeRepository.save(Place(
            id = EntityIdGenerator.generateRandom(),
            name = placeName,
            location = building.location,
            building = building,
            siGunGuId = building.siGunGuId,
            eupMyeonDongId = building.eupMyeonDongId,
        ))
    }

    fun createBuilding(
        location: Location = Location(127.5, 37.5),
        eupMyeonDongId: String = eupMyeonDongById.keys.first(),
        siGunGuId: String = siGunGuById.keys.first(),
    ): Building {
        return buildingRepository.save(Building(
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
        ))
    }

    fun createBuildingAndPlace(
        placeName: String = "장소장소",
        location: Location = Location(127.5, 37.5),
        building: Building? = null,
        eupMyeonDongId: String = eupMyeonDongById.keys.first(),
        siGunGuId: String = siGunGuById.keys.first(),
    ): Place {
        val buildingToUse = building ?: buildingRepository.save(Building(
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
        ))
        return createPlace(placeName, buildingToUse)
    }

    fun registerPlaceAccessibility(place: Place, user: User? = null): PlaceAccessibility {
        return placeAccessibilityRepository.save(
            PlaceAccessibility(
                id = EntityIdGenerator.generateRandom(),
                placeId = place.id,
                isFirstFloor = true,
                stairInfo = StairInfo.NONE,
                hasSlope = true,
                imageUrls = emptyList(),
                userId = user?.id,
                createdAt = clock.instant(),
            ),
        )
    }

    fun registerBuildingAccessibility(building: Building, user: User? = null): BuildingAccessibility {
        return buildingAccessibilityRepository.save(
            BuildingAccessibility(
                id = EntityIdGenerator.generateRandom(),
                buildingId = building.id,
                entranceStairInfo = StairInfo.NONE,
                hasSlope = true,
                hasElevator = true,
                elevatorStairInfo = StairInfo.NONE,
                imageUrls = emptyList(),
                userId = user?.id,
                createdAt = clock.instant(),
            ),
        )
    }

    fun registerBuildingAndPlaceAccessibility(place: Place, user: User? = null): Pair<PlaceAccessibility, BuildingAccessibility> {
        return Pair(
            registerPlaceAccessibility(place, user),
            registerBuildingAccessibility(place.building!!, user),
        )
    }

    fun registerBuildingAccessibilityComment(building: Building, comment: String, user: User? = null): BuildingAccessibilityComment {
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

    fun registerPlaceAccessibilityComment(place: Place, comment: String, user: User? = null): PlaceAccessibilityComment {
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

    fun giveBuildingAccessibilityUpvote(buildingAccessibility: BuildingAccessibility, user: User = createUser()) {
        buildingAccessibilityUpvoteRepository.save(
            BuildingAccessibilityUpvote(
                id = EntityIdGenerator.generateRandom(),
                userId = user.id,
                buildingAccessibility = buildingAccessibility,
                createdAt = clock.instant(),
            ),
        )
    }

    private fun generateRandomString(length: Int): String {
        return Base64.getEncoder().encodeToString(Random.nextBytes(length)).take(length)
    }
}
