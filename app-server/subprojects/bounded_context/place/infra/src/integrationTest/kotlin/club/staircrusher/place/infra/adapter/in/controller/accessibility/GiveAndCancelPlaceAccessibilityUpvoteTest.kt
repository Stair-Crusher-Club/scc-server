package club.staircrusher.place.infra.adapter.`in`.controller.accessibility

import club.staircrusher.api.spec.dto.CancelPlaceAccessibilityUpvoteRequestDto
import club.staircrusher.api.spec.dto.GivePlaceAccessibilityUpvoteRequestDto
import club.staircrusher.place.application.port.out.accessibility.SlackService
import club.staircrusher.place.application.port.out.accessibility.persistence.PlaceAccessibilityUpvoteRepository
import club.staircrusher.place.infra.adapter.`in`.controller.accessibility.base.AccessibilityITBase
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean

class GiveAndCancelPlaceAccessibilityUpvoteTest : AccessibilityITBase() {
    @Autowired
    lateinit var placeAccessibilityUpvoteRepository: PlaceAccessibilityUpvoteRepository
    @MockBean
    lateinit var slackService: SlackService

    @Test
    fun cancelBuildingAccessibilityUpvoteTest() {
        val (user, placeAccessibility) = transactionManager.doInTransaction {
            val user = testDataGenerator.createIdentifiedUser()
            val place = testDataGenerator.createBuildingAndPlace()
            val placeAccessibility = testDataGenerator.registerPlaceAccessibility(userAccount = user.account, place = place)
            user to placeAccessibility
        }

        val giveUpvoteParams = GivePlaceAccessibilityUpvoteRequestDto(placeAccessibilityId = placeAccessibility.id)
        mvc
            .sccRequest("/givePlaceAccessibilityUpvote", giveUpvoteParams, userAccount = user.account)
            .andExpect {
                transactionManager.doInTransaction {
                    assertNotNull(
                        placeAccessibilityUpvoteRepository.findExistingUpvote(user.account.id, placeAccessibility.id)
                    )
                }
            }
            .apply {
                verify(slackService, times(1)).send(
                    channel = eq("#scc-accessibility-report-test"),
                    any(),
                )
            }

        val cancelUpvoteParams = CancelPlaceAccessibilityUpvoteRequestDto(
            placeAccessibilityId = placeAccessibility.id
        )
        mvc
            .sccRequest("/cancelPlaceAccessibilityUpvote", cancelUpvoteParams, userAccount = user.account)
            .andExpect {
                transactionManager.doInTransaction {
                    assertNull(
                        placeAccessibilityUpvoteRepository.findExistingUpvote(user.account.id, placeAccessibility.id)
                    )
                }
            }
    }
}
