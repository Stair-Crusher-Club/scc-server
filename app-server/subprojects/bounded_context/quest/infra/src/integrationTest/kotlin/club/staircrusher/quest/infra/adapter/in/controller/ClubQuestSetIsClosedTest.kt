package club.staircrusher.quest.infra.adapter.`in`.controller

import club.staircrusher.admin_api.spec.dto.ClubQuestsClubQuestIdIsClosedPutRequest
import club.staircrusher.admin_api.spec.dto.CreateClubQuestResponseDTO
import club.staircrusher.place.application.port.out.persistence.PlaceRepository
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod

class ClubQuestSetIsClosedTest : ClubQuestITBase() {
    @Autowired
    private lateinit var placeRepository: PlaceRepository

    @BeforeEach
    fun setUp() = transactionManager.doInTransaction {
        clubQuestRepository.removeAll()
        clubQuestTargetBuildingRepository.removeAll()
        clubQuestTargetPlaceRepository.removeAll()
    }

    @Test
    fun `정상적인 경우`() {
        val placeId = transactionManager.doInTransaction {
            testDataGenerator.createBuildingAndPlace().id
        }
        val createClubQuestRequestBody = getCreateClubQuestRequestBody(placeId = placeId)

        val clubQuest = mvc
            .sccAdminRequest("/admin/clubQuests/create", HttpMethod.POST, createClubQuestRequestBody)
            .run {
                val result = getResult(CreateClubQuestResponseDTO::class)
                val clubQuestId = result.clubQuestIds[0]
                transactionManager.doInTransaction {
                    clubQuestRepository.findById(clubQuestId)
                }
            }
        val targetPlaceVO = clubQuest.targetBuildings[0].places[0]

        val requestBody = ClubQuestsClubQuestIdIsClosedPutRequest(
            buildingId = targetPlaceVO.buildingId,
            placeId = targetPlaceVO.placeId,
            isClosed = true,
        )
        mvc
            .sccAdminRequest("/admin/clubQuests/${clubQuest.id}/isClosed", HttpMethod.PUT, requestBody)
            .apply {
                transactionManager.doInTransaction {
                    val reloadedClubQuest = clubQuestRepository.findById(clubQuest.id)
                    val reloadedTargetPlaceVO = reloadedClubQuest.targetBuildings[0].places[0]
                    assertTrue(reloadedTargetPlaceVO.isClosed)
                    assertFalse(reloadedTargetPlaceVO.isNotAccessible)

                    val targetPlace = clubQuestTargetPlaceRepository.findByClubQuestIdAndPlaceId(
                        clubQuestId = clubQuest.id,
                        placeId = reloadedTargetPlaceVO.placeId,
                    )
                    assertNotNull(targetPlace)
                    assertTrue(targetPlace!!.isClosed)
                    assertFalse(targetPlace.isNotAccessible)

                    val place = placeRepository.findById(targetPlace.placeId)
                    assertTrue(place.isClosed)
                    assertFalse(place.isNotAccessible)
                }
            }
    }
}
