package club.staircrusher.accesssibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityUpvoteRepository
import club.staircrusher.accesssibility.infra.adapter.`in`.controller.base.AccessibilityITBase
import club.staircrusher.api.spec.dto.GiveBuildingAccessibilityUpvoteRequestDto
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
            val user = testDataGenerator.createUser()
            val place = testDataGenerator.createBuildingAndPlace()
            val buildingAccessibility =
                testDataGenerator.registerBuildingAndPlaceAccessibility(user = user, place = place).second
            Pair(user, buildingAccessibility)
        }

        val giveUpvoteParams = GiveBuildingAccessibilityUpvoteRequestDto(
            buildingAccessibilityId = buildingAccessibility.id
        )
        mvc
            .sccRequest("/giveBuildingAccessibilityUpvote", giveUpvoteParams, user = user)
            .andExpect {
                transactionManager.doInTransaction {
                    assertNotNull(
                        buildingAccessibilityUpvoteRepository.findExistingUpvote(
                            user.id,
                            buildingAccessibility.id
                        )
                    )
                }
            }

        val cancelUpvoteParams = GiveBuildingAccessibilityUpvoteRequestDto(
            buildingAccessibilityId = buildingAccessibility.id
        )
        mvc
            .sccRequest("/cancelBuildingAccessibilityUpvote", cancelUpvoteParams, user = user)
            .andExpect {
                transactionManager.doInTransaction {
                    assertNull(
                        buildingAccessibilityUpvoteRepository.findExistingUpvote(
                            user.id,
                            buildingAccessibility.id
                        )
                    )
                }
            }
    }
}
