package club.staircrusher.quest.application.port.`in`

import club.staircrusher.place.application.port.out.persistence.PlaceRepository
import club.staircrusher.quest.application.port.out.persistence.ClubQuestRepository
import club.staircrusher.quest.application.port.out.persistence.ClubQuestTargetPlaceRepository
import club.staircrusher.quest.application.port.out.web.ClubQuestTargetPlacesSearcher
import club.staircrusher.quest.domain.model.ClubQuest
import club.staircrusher.quest.domain.model.ClubQuestCreateDryRunResultItem
import club.staircrusher.quest.domain.model.ClubQuestPurposeType
import club.staircrusher.quest.domain.model.DryRunnedClubQuestTargetBuilding
import club.staircrusher.quest.domain.model.DryRunnedClubQuestTargetPlace
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.testing.spring_it.ITDataGenerator
import club.staircrusher.testing.spring_it.base.SccSpringITApplication
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.stub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import java.time.Duration

@SpringBootTest(classes = [SccSpringITApplication::class], webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CrossValidateClubQuestPlacesUseCaseTest {
    @Autowired
    private lateinit var sut: CrossValidateClubQuestPlacesUseCase

    @Autowired
    private lateinit var transactionManager: TransactionManager

    @Autowired
    private lateinit var dataGenerator: ITDataGenerator

    @Autowired
    private lateinit var clubQuestRepository: ClubQuestRepository

    @MockBean
    private lateinit var clubQuestTargetPlacesSearcher: ClubQuestTargetPlacesSearcher

    @Autowired
    private lateinit var clubQuestTargetPlaceRepository: ClubQuestTargetPlaceRepository

    @Autowired
    private lateinit var placeRepository: PlaceRepository

    @Test
    fun `정상적인 경우`() {
        val (place1, place2, clubQuest) = transactionManager.doInTransaction {
            val place1 = dataGenerator.createBuildingAndPlace()
            val building = place1.building
            val place2 = dataGenerator.createPlace(building = building)
            val now = SccClock.instant()
            val clubQuest = ClubQuest.of(
                name = "quest",
                purposeType = ClubQuestPurposeType.CRUSHER_CLUB,
                startAt = now,
                endAt = now + Duration.ofDays(7),
                dryRunResultItem = ClubQuestCreateDryRunResultItem(
                    questNamePostfix = "1",
                    questCenterLocation = building.location,
                    targetBuildings = listOf(
                        DryRunnedClubQuestTargetBuilding(
                            buildingId = building.id,
                            name = building.name ?: "",
                            location = building.location,
                            places = listOf(
                                DryRunnedClubQuestTargetPlace(
                                    buildingId = building.id,
                                    placeId = place1.id,
                                    name = place1.name,
                                    location = place1.location,
                                ),
                                DryRunnedClubQuestTargetPlace(
                                    buildingId = building.id,
                                    placeId = place2.id,
                                    name = place2.name,
                                    location = place2.location,
                                ),
                            ),
                        ),
                    ),
                ),
                createdAt = now,
            )
            clubQuestRepository.save(clubQuest)
            Triple(place1, place2, clubQuest)
        }

        // given - place1은 invalid, place2는 valid
        // how to mock suspend function: https://github.com/mockito/mockito-kotlin/issues/311#issuecomment-454183020
        clubQuestTargetPlacesSearcher.stub {
            onBlocking { crossValidatePlaces(listOf(place1, place2)) }.thenReturn(listOf(false, true))
        }

        // when
        sut.handle(clubQuest.id)

        // then - targetPlace1의 isClosedExpected만 true이고, targetPlace2와 place1, place2는 변한 게 없다.
        transactionManager.doInTransaction {
            val targetPlaces = clubQuestTargetPlaceRepository.findByClubQuestIdAndPlaceIdIn(
                clubQuestId = clubQuest.id,
                placeIds = listOf(place1.id, place2.id),
            )
            assertTrue(targetPlaces.find { it.placeId == place1.id }!!.isClosedExpected)
            assertFalse(targetPlaces.find { it.placeId == place2.id }!!.isClosedExpected)

            val places = placeRepository.findByIdIn(listOf(place1.id, place2.id))
            assertFalse(places.find { it.id == place1.id }!!.isClosed)
            assertFalse(places.find { it.id == place2.id }!!.isClosed)
        }
    }
}
