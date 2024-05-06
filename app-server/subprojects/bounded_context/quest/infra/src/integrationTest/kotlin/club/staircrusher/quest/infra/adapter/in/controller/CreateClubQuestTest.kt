package club.staircrusher.quest.infra.adapter.`in`.controller

import club.staircrusher.admin_api.spec.dto.CreateClubQuestResponseDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod

class CreateClubQuestTest : ClubQuestITBase() {
    @BeforeEach
    fun setUp() = transactionManager.doInTransaction {
        clubQuestRepository.removeAll()
        clubQuestTargetBuildingRepository.removeAll()
        clubQuestTargetPlaceRepository.removeAll()
    }

    @Test
    fun `dual write test`() {
        val requestBody = getCreateClubQuestRequestBody()
        mvc
            .sccAdminRequest("/admin/clubQuests/create", HttpMethod.POST, requestBody)
            .apply {
                val result = getResult(CreateClubQuestResponseDTO::class)
                val clubQuestId = result.clubQuestIds[0]
                val clubQuest = clubQuestRepository.findById(clubQuestId)
                val targetBuildingVO = clubQuest.targetBuildings[0]
                val targetPlaceVO = targetBuildingVO.places[0]

                val targetBuilding = clubQuestTargetBuildingRepository.findByClubQuestIdAndBuildingId(
                    clubQuestId = clubQuestId,
                    buildingId = targetBuildingVO.buildingId,
                )
                assertNotNull(targetBuilding)
                assertEquals(targetBuildingVO.buildingId, targetBuilding!!.buildingId)
                assertEquals(targetBuildingVO.name, targetBuilding.name)
                assertEquals(targetBuildingVO.location, targetBuilding.location)

                val targetPlace = clubQuestTargetPlaceRepository.findByClubQuestIdAndPlaceId(
                    clubQuestId = clubQuestId,
                    placeId = targetPlaceVO.placeId,
                )
                assertNotNull(targetPlace)
                assertEquals(targetPlaceVO.buildingId, targetPlace!!.buildingId)
                assertEquals(targetPlaceVO.placeId, targetPlace.placeId)
                assertEquals(targetPlaceVO.name, targetPlace.name)
                assertEquals(targetPlaceVO.location, targetPlace.location)
                assertEquals(targetPlaceVO.isClosed, targetPlace.isClosed)
                assertEquals(targetPlaceVO.isNotAccessible, targetPlace.isNotAccessible)
            }
    }
}
