package club.staircrusher.accesssibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.BuildingAccessibilityComment
import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.accessibility.domain.model.PlaceAccessibilityComment
import club.staircrusher.accessibility.infra.adapter.`in`.controller.toModel
import club.staircrusher.accesssibility.infra.adapter.`in`.controller.base.AccessibilityITBase
import club.staircrusher.api.spec.dto.GetAccessibilityPost200Response
import club.staircrusher.api.spec.dto.GetAccessibilityPostRequest
import club.staircrusher.place.domain.model.Place
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GetAccessibilityTest : AccessibilityITBase() {
    @Test
    fun getAccessibilityTest() {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createUser()
        }
        val (place, placeAccessibility, buildingAccessibility, placeAccessibilityComment, buildingAccessibilityComment) = transactionManager.doInTransaction {
            val place = testDataGenerator.createBuildingAndPlace(placeName = "장소장소")
            val (placeAccessibility, buildingAccessibility) = testDataGenerator.registerBuildingAndPlaceAccessibility(place, user)

            repeat(2) {
                testDataGenerator.giveBuildingAccessibilityUpvote(buildingAccessibility)
            }
            testDataGenerator.giveBuildingAccessibilityUpvote(buildingAccessibility, user)

            val buildingAccessibilityComment = testDataGenerator.registerBuildingAccessibilityComment(place.building!!, "건물 코멘트")
            val placeAccessibilityComment = testDataGenerator.registerPlaceAccessibilityComment(place, "장소 코멘트", user)

            data class Result(
                val place: Place,
                val placeAccessibility: PlaceAccessibility,
                val buildingAccessibility: BuildingAccessibility,
                val placeAccessibilityComment: PlaceAccessibilityComment,
                val buildingAccessibilityComment: BuildingAccessibilityComment,
            )
            Result(place, placeAccessibility, buildingAccessibility, placeAccessibilityComment, buildingAccessibilityComment)
        }

        val params = GetAccessibilityPostRequest(
            placeId = place.id
        )
        mvc
            .sccRequest("/getAccessibility", params, user = user)
            .apply {
                val result = getResult(GetAccessibilityPost200Response::class)
                assertEquals(buildingAccessibility.id, result.buildingAccessibility!!.id)
                assertEquals(buildingAccessibility.buildingId, result.buildingAccessibility!!.buildingId)
                assertEquals(buildingAccessibility.entranceStairInfo, result.buildingAccessibility!!.entranceStairInfo.toModel())
                assertEquals(buildingAccessibility.hasSlope, result.buildingAccessibility!!.hasSlope)
                assertEquals(buildingAccessibility.hasElevator, result.buildingAccessibility!!.hasElevator)
                assertEquals(buildingAccessibility.elevatorStairInfo, result.buildingAccessibility!!.elevatorStairInfo.toModel())
                assertEquals(user.nickname, result.buildingAccessibility!!.registeredUserName)
                assertTrue(result.buildingAccessibility!!.isUpvoted)
                assertEquals(3, result.buildingAccessibility!!.totalUpvoteCount)
                assertEquals(1, result.buildingAccessibilityComments.size)
                assertEquals(buildingAccessibilityComment.id, result.buildingAccessibilityComments[0].id)
                assertEquals(buildingAccessibilityComment.buildingId, result.buildingAccessibilityComments[0].buildingId)
                assertNull(result.buildingAccessibilityComments[0].user)
                assertEquals(buildingAccessibilityComment.comment, result.buildingAccessibilityComments[0].comment)
                assertEquals(buildingAccessibilityComment.createdAt.toEpochMilli(), result.buildingAccessibilityComments[0].createdAt.value)

                assertEquals(placeAccessibility.id, result.placeAccessibility!!.id)
                assertEquals(placeAccessibility.placeId, result.placeAccessibility!!.placeId)
                assertEquals(placeAccessibility.isFirstFloor, result.placeAccessibility!!.isFirstFloor)
                assertEquals(placeAccessibility.stairInfo, result.placeAccessibility!!.stairInfo.toModel())
                assertEquals(placeAccessibility.hasSlope, result.placeAccessibility!!.hasSlope)
                assertEquals(user.nickname, result.placeAccessibility!!.registeredUserName)
                assertEquals(1, result.placeAccessibilityComments.size)
                assertEquals(placeAccessibilityComment.id, result.placeAccessibilityComments[0].id)
                assertEquals(placeAccessibilityComment.placeId, result.placeAccessibilityComments[0].placeId)
                assertNotNull(result.placeAccessibilityComments[0].user)
                assertEquals(placeAccessibilityComment.comment, result.placeAccessibilityComments[0].comment)
                assertEquals(placeAccessibilityComment.createdAt.toEpochMilli(), result.placeAccessibilityComments[0].createdAt.value)

                assertFalse(result.hasOtherPlacesToRegisterInBuilding)
            }

        transactionManager.doInTransaction {
            testDataGenerator.createPlace(placeName = "장소장소 2", building = place.building!!)
        }
        mvc
            .sccRequest("/getAccessibility", params, user = user)
            .apply {
                val result = getResult(GetAccessibilityPost200Response::class)
                assertTrue(result.hasOtherPlacesToRegisterInBuilding)
            }
    }

    // TODO: 유저 없는 경우도 테스트?
}
