package club.staircrusher.accesssibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityUpvoteRepository
import club.staircrusher.accesssibility.infra.adapter.`in`.controller.base.AccessibilityITBase
import club.staircrusher.api.spec.dto.CancelPlaceAccessibilityUpvoteRequestDto
import club.staircrusher.api.spec.dto.GivePlaceAccessibilityUpvoteRequestDto
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class GiveAndCancelPlaceAccessibilityUpvoteTest : AccessibilityITBase() {
    @Autowired
    lateinit var placeAccessibilityUpvoteRepository: PlaceAccessibilityUpvoteRepository

    @Test
    fun cancelBuildingAccessibilityUpvoteTest() {
        val (user, placeAccessibility) = transactionManager.doInTransaction {
            val user = testDataGenerator.createUser()
            val place = testDataGenerator.createBuildingAndPlace()
            val placeAccessibility = testDataGenerator.registerPlaceAccessibility(user = user, place = place)
            user to placeAccessibility
        }

        val giveUpvoteParams = GivePlaceAccessibilityUpvoteRequestDto(placeAccessibilityId = placeAccessibility.id)
        mvc
            .sccRequest("/givePlaceAccessibilityUpvote", giveUpvoteParams, user = user)
            .andExpect {
                transactionManager.doInTransaction {
                    assertNotNull(
                        placeAccessibilityUpvoteRepository.findExistingUpvote(user.id, placeAccessibility.id)
                    )
                }
            }

        val cancelUpvoteParams = CancelPlaceAccessibilityUpvoteRequestDto(
            placeAccessibilityId = placeAccessibility.id
        )
        mvc
            .sccRequest("/cancelPlaceAccessibilityUpvote", cancelUpvoteParams, user = user)
            .andExpect {
                transactionManager.doInTransaction {
                    assertNull(
                        placeAccessibilityUpvoteRepository.findExistingUpvote(user.id, placeAccessibility.id)
                    )
                }
            }
    }
}
