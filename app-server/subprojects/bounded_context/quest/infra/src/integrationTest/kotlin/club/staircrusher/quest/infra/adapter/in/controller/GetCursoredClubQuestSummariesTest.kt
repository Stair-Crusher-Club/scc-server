package club.staircrusher.quest.infra.adapter.`in`.controller

import club.staircrusher.admin_api.spec.dto.CreateClubQuestResponseDTO
import club.staircrusher.admin_api.spec.dto.GetCursoredClubQuestSummariesResultDTO
import club.staircrusher.quest.domain.model.ClubQuest
import club.staircrusher.testing.spring_it.mock.MockSccClock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import java.time.Duration

class GetCursoredClubQuestSummariesTest : ClubQuestITBase() {
    @BeforeEach
    fun setUp() = transactionManager.doInTransaction {
        clubQuestRepository.deleteAll()
    }

    @Autowired
    private lateinit var mockSccClock: MockSccClock

    @Test
    fun `정상적인 경우`() {
        // given - 퀘스트 2개
        val clubQuest1 = createClubQuest()
        mockSccClock.advanceTime(Duration.ofSeconds(1))
        val clubQuest2 = createClubQuest()
        mockSccClock.advanceTime(Duration.ofSeconds(1))

        val cursor = mvc
            .sccAdminRequest("/admin/clubQuestSummaries/cursored?limit=1", HttpMethod.GET, requestBody = null)
            .run {
                val result = getResult<GetCursoredClubQuestSummariesResultDTO>()
                assertNotNull(result.cursor)
                assertEquals(1, result.list.size)
                assertEquals(clubQuest2.id, result.list[0].id)

                result.cursor
            }
        mvc
            .sccAdminRequest("/admin/clubQuestSummaries/cursored?limit=1&cursor=$cursor", HttpMethod.GET, requestBody = null)
            .apply {
                val result = getResult<GetCursoredClubQuestSummariesResultDTO>()
                assertNull(result.cursor)
                assertEquals(1, result.list.size)
                assertEquals(clubQuest1.id, result.list[0].id)
            }
    }

    private fun createClubQuest(): ClubQuest {
        val placeId = transactionManager.doInTransaction {
            testDataGenerator.createBuildingAndPlace().id
        }
        val createClubQuestRequestBody = getCreateClubQuestRequestBody(placeId = placeId)
        return mvc
            .sccAdminRequest("/admin/clubQuests/create", HttpMethod.POST, createClubQuestRequestBody)
            .run {
                val result = getResult(CreateClubQuestResponseDTO::class)
                val clubQuestId = result.clubQuestIds[0]
                transactionManager.doInTransaction {
                    clubQuestRepository.findById(clubQuestId).get()
                }
            }
    }
}
