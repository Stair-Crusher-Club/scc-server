package club.staircrusher.place.infra.adapter.`in`.controller.accessibility

import club.staircrusher.api.spec.dto.GiveBuildingAccessibilityUpvoteRequestDto
import club.staircrusher.place.application.port.out.accessibility.persistence.BuildingAccessibilityUpvoteRepository
import club.staircrusher.place.infra.adapter.`in`.controller.accessibility.base.AccessibilityITBase
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class GiveAndCancelBuildingAccessibilityUpvoteTest : AccessibilityITBase() {
    @Autowired
    lateinit var buildingAccessibilityUpvoteRepository: BuildingAccessibilityUpvoteRepository

    @Test
    fun cancelBuildingAccessibilityUpvoteTest() {
        val (user, buildingAccessibility) = transactionManager.doInTransaction {
            val user = testDataGenerator.createIdentifiedUser()
            val place = testDataGenerator.createBuildingAndPlace()
            val buildingAccessibility =
                testDataGenerator.registerBuildingAndPlaceAccessibility(userAccount = user.account, place = place).second
            Pair(user, buildingAccessibility)
        }

        val giveUpvoteParams = GiveBuildingAccessibilityUpvoteRequestDto(
            buildingAccessibilityId = buildingAccessibility.id
        )
        mvc
            .sccRequest("/giveBuildingAccessibilityUpvote", giveUpvoteParams, userAccount = user.account)
            .andExpect {
                transactionManager.doInTransaction {
                    assertNotNull(
                        buildingAccessibilityUpvoteRepository.findExistingUpvote(
                            user.account.id,
                            buildingAccessibility.id
                        )
                    )
                }
            }

        val cancelUpvoteParams = GiveBuildingAccessibilityUpvoteRequestDto(
            buildingAccessibilityId = buildingAccessibility.id
        )
        mvc
            .sccRequest("/cancelBuildingAccessibilityUpvote", cancelUpvoteParams, userAccount = user.account)
            .andExpect {
                transactionManager.doInTransaction {
                    assertNull(
                        buildingAccessibilityUpvoteRepository.findExistingUpvote(
                            user.account.id,
                            buildingAccessibility.id
                        )
                    )
                }
            }
    }
}
