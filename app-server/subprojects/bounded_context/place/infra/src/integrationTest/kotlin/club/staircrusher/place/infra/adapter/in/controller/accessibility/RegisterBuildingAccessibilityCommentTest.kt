package club.staircrusher.place.infra.adapter.`in`.controller.accessibility

import club.staircrusher.api.spec.dto.RegisterBuildingAccessibilityCommentPostRequest
import club.staircrusher.place.application.port.out.accessibility.persistence.BuildingAccessibilityCommentRepository
import club.staircrusher.place.infra.adapter.`in`.controller.accessibility.base.AccessibilityITBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration

class RegisterBuildingAccessibilityCommentTest : AccessibilityITBase() {

    @Autowired
    private lateinit var buildingAccessibilityCommentRepository: BuildingAccessibilityCommentRepository

    @Test
    fun testRegisterBuildingAccessibilityComment() {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createIdentifiedUser()
        }
        val place = transactionManager.doInTransaction {
            testDataGenerator.createBuildingAndPlace()
        }

        run {
            val params = RegisterBuildingAccessibilityCommentPostRequest(
                buildingId = place.building.id,
                comment = "실명 코멘트",
            )
            mvc.sccRequest("/registerBuildingAccessibilityComment", params, userAccount = user.account).andReturn()
        }
        clock.advanceTime(Duration.ofSeconds(1))

        val comments = transactionManager.doInTransaction {
            buildingAccessibilityCommentRepository.findByBuildingId(place.building.id)
        }.sortedByDescending { it.createdAt }

        assertEquals(1, comments.size)
        assertEquals(place.building.id, comments[0].buildingId)
        assertEquals("실명 코멘트", comments[0].comment)
        assertEquals(user.account.id, comments[0].userId)
        assertEquals((clock.instant() - Duration.ofSeconds(1)).toEpochMilli(), comments[0].createdAt.toEpochMilli())
    }
}
