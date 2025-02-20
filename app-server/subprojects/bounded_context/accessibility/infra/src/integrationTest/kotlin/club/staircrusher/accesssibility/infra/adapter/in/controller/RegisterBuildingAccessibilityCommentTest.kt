package club.staircrusher.accesssibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityCommentRepository
import club.staircrusher.accesssibility.infra.adapter.`in`.controller.base.AccessibilityITBase
import club.staircrusher.api.spec.dto.RegisterBuildingAccessibilityCommentPostRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
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
        val result = run {
            val params = RegisterBuildingAccessibilityCommentPostRequest(
                buildingId = place.building.id,
                comment = "익명 코멘트",
            )
            mvc.sccRequest("/registerBuildingAccessibilityComment", params).andReturn()
        }

        val comments = transactionManager.doInTransaction {
            buildingAccessibilityCommentRepository.findByBuildingId(place.building.id)
        }.sortedByDescending { it.createdAt }

        assertEquals(2, comments.size)
        assertEquals(place.building.id, comments[0].buildingId)
        assertEquals("익명 코멘트", comments[0].comment)
        assertNull(comments[0].userId)
        assertEquals(clock.millis(), comments[0].createdAt.toEpochMilli())
        assertEquals(place.building.id, comments[1].buildingId)
        assertEquals("실명 코멘트", comments[1].comment)
        assertEquals(user.id, comments[1].userId)
        assertEquals((clock.instant() - Duration.ofSeconds(1)).toEpochMilli(), comments[1].createdAt.toEpochMilli())
    }
}
