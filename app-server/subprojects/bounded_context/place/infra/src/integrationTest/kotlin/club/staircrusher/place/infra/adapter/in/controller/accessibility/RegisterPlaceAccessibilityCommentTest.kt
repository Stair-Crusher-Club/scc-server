package club.staircrusher.place.infra.adapter.`in`.controller.accessibility

import club.staircrusher.api.spec.dto.RegisterPlaceAccessibilityCommentPostRequest
import club.staircrusher.place.application.port.out.accessibility.persistence.PlaceAccessibilityCommentRepository
import club.staircrusher.place.infra.adapter.`in`.controller.accessibility.base.AccessibilityITBase
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration

class RegisterPlaceAccessibilityCommentTest : AccessibilityITBase() {

    @Autowired
    private lateinit var placeAccessibilityCommentRepository: PlaceAccessibilityCommentRepository

    @Test
    fun testRegisterPlaceAccessibilityComment() {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createIdentifiedUser()
        }
        val place = transactionManager.doInTransaction {
            testDataGenerator.createBuildingAndPlace()
        }

        run {
            val params = RegisterPlaceAccessibilityCommentPostRequest(
                placeId = place.id,
                comment = "실명 코멘트",
            )
            mvc.sccRequest("/registerPlaceAccessibilityComment", params, userAccount = user.account).andReturn()
        }
        clock.advanceTime(Duration.ofSeconds(1))
        val result = run {
            val params = RegisterPlaceAccessibilityCommentPostRequest(
                placeId = place.id,
                comment = "익명 코멘트",
            )
            mvc.sccAnonymousRequest("/registerPlaceAccessibilityComment", params).andReturn()
        }

        val comments = transactionManager.doInTransaction {
            placeAccessibilityCommentRepository.findByPlaceId(place.id)
        }.sortedByDescending { it.createdAt }

        Assertions.assertEquals(2, comments.size)
        Assertions.assertEquals(place.id, comments[0].placeId)
        Assertions.assertEquals("익명 코멘트", comments[0].comment)
        Assertions.assertNotNull(comments[0].userId)
        Assertions.assertEquals(clock.millis(), comments[0].createdAt.toEpochMilli())
        Assertions.assertEquals(place.id, comments[1].placeId)
        Assertions.assertEquals("실명 코멘트", comments[1].comment)
        Assertions.assertEquals(user.account.id, comments[1].userId)
        Assertions.assertEquals((clock.instant() - Duration.ofSeconds(1)).toEpochMilli(), comments[1].createdAt.toEpochMilli())
    }
}
