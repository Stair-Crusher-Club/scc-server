package club.staircrusher.accesssibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.domain.model.AccessibilityImage
import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.BuildingAccessibilityComment
import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.accessibility.domain.model.PlaceAccessibilityComment
import club.staircrusher.accessibility.infra.adapter.`in`.controller.toModel
import club.staircrusher.accesssibility.infra.adapter.`in`.controller.base.AccessibilityITBase
import club.staircrusher.api.spec.dto.AccessibilityInfoDto
import club.staircrusher.api.spec.dto.GetAccessibilityPostRequest
import club.staircrusher.place.domain.model.Building
import club.staircrusher.place.domain.model.Place
import club.staircrusher.testing.spring_it.mock.MockSccClock
import club.staircrusher.user.domain.model.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration

class GetAccessibilityTest : AccessibilityITBase() {
    @Autowired
    lateinit var mockSccClock: MockSccClock

    @Test
    fun getAccessibilityTest() {
        val (user, place, placeAccessibility, buildingAccessibility, placeAccessibilityComment, buildingAccessibilityComment) = registerAccessibility()

        val params = GetAccessibilityPostRequest(
            placeId = place.id
        )
        mvc
            .sccRequest("/getAccessibility", params, user = user)
            .apply {
                val result = getResult(AccessibilityInfoDto::class)
                assertEquals(buildingAccessibility.id, result.buildingAccessibility!!.id)
                assertEquals(buildingAccessibility.buildingId, result.buildingAccessibility!!.buildingId)
                assertEquals(
                    buildingAccessibility.entranceStairInfo,
                    result.buildingAccessibility!!.entranceStairInfo.toModel()
                )
                assertEquals(buildingAccessibility.hasSlope, result.buildingAccessibility!!.hasSlope)
                assertEquals(buildingAccessibility.hasElevator, result.buildingAccessibility!!.hasElevator)
                assertEquals(
                    buildingAccessibility.elevatorStairInfo,
                    result.buildingAccessibility!!.elevatorStairInfo.toModel()
                )
                assertEquals(user.nickname, result.buildingAccessibility!!.registeredUserName)
                assertTrue(result.buildingAccessibility!!.isUpvoted)
                assertEquals(3, result.buildingAccessibility!!.totalUpvoteCount)
                assertEquals(1, result.buildingAccessibilityComments.size)
                assertEquals(buildingAccessibilityComment.id, result.buildingAccessibilityComments[0].id)
                assertEquals(
                    buildingAccessibilityComment.buildingId,
                    result.buildingAccessibilityComments[0].buildingId
                )
                assertNull(result.buildingAccessibilityComments[0].user)
                assertEquals(buildingAccessibilityComment.comment, result.buildingAccessibilityComments[0].comment)
                assertEquals(
                    buildingAccessibilityComment.createdAt.toEpochMilli(),
                    result.buildingAccessibilityComments[0].createdAt.value
                )

                assertEquals(placeAccessibility.id, result.placeAccessibility!!.id)
                assertEquals(placeAccessibility.placeId, result.placeAccessibility!!.placeId)
                assertEquals(placeAccessibility.isFirstFloor, result.placeAccessibility!!.isFirstFloor)
                assertEquals(placeAccessibility.stairInfo, result.placeAccessibility!!.stairInfo.toModel())
                assertEquals(placeAccessibility.hasSlope, result.placeAccessibility!!.hasSlope)
                assertTrue(result.placeAccessibility!!.deletionInfo!!.isLastInBuilding)

                assertEquals(user.nickname, result.placeAccessibility!!.registeredUserName)
                assertEquals(1, result.placeAccessibilityComments.size)
                assertEquals(placeAccessibilityComment.id, result.placeAccessibilityComments[0].id)
                assertEquals(placeAccessibilityComment.placeId, result.placeAccessibilityComments[0].placeId)
                assertNotNull(result.placeAccessibilityComments[0].user)
                assertEquals(placeAccessibilityComment.comment, result.placeAccessibilityComments[0].comment)
                assertEquals(
                    placeAccessibilityComment.createdAt.toEpochMilli(),
                    result.placeAccessibilityComments[0].createdAt.value
                )
                assertEquals(result.totalFavoriteCount, 0)
                assertEquals(result.isFavoritePlace, false)

                assertFalse(result.hasOtherPlacesToRegisterInBuilding)

            }

        transactionManager.doInTransaction {
            testDataGenerator.createPlace(placeName = "장소장소 2", building = place.building)
        }
        mvc
            .sccRequest("/getAccessibility", params, user = user)
            .apply {
                val result = getResult(AccessibilityInfoDto::class)
                assertTrue(result.placeAccessibility!!.deletionInfo!!.isLastInBuilding)
                assertTrue(result.hasOtherPlacesToRegisterInBuilding)
            }
    }

    @Test
    fun `로그인되어 있지 않아도 잘 동작한다`() {
        val place = registerAccessibility().place
        val params = GetAccessibilityPostRequest(
            placeId = place.id
        )
        mvc
            .sccRequest("/getAccessibility", params) // 인증 없이 요청한다.
            .andExpect {
                status {
                    isOk()
                }
            }
            .apply {
                val result = getResult(AccessibilityInfoDto::class)
                assertNull(result.placeAccessibility!!.deletionInfo)
            }
    }

    @Test
    fun `장소 정보를 등록한 본인이 아닌 사람이 접근성 조회를 조회하면 삭제 불가능하다`() {
        val place = registerAccessibility().place
        val params = GetAccessibilityPostRequest(
            placeId = place.id
        )
        val otherUser = transactionManager.doInTransaction {
            testDataGenerator.createUser()
        }
        mvc
            .sccRequest("/getAccessibility", params, user = otherUser) // 타인이 조회한다.
            .andExpect {
                status {
                    isOk()
                }
            }
            .apply {
                val result = getResult(AccessibilityInfoDto::class)
                assertNull(result.placeAccessibility!!.deletionInfo)
            }
    }

    @Test
    fun `한 건물에 두 개 이상의 장소 정보가 존재하면 삭제는 가능하지만 isLastInBuilding은 false이다`() {
        val (user, place1) = registerAccessibility()
        val building = place1.building
        registerAccessibility(overridingBuilding = building)
        val params = GetAccessibilityPostRequest(
            placeId = place1.id
        )
        mvc
            .sccRequest("/getAccessibility", params, user = user)
            .andExpect {
                status {
                    isOk()
                }
            }
            .apply {
                val result = getResult(AccessibilityInfoDto::class)
                assertFalse(result.placeAccessibility!!.deletionInfo!!.isLastInBuilding)
            }
    }

    @Test
    fun `장소 정보는 등록한지 6시간까지만 삭제 가능하다`() {
        val (user, place1) = registerAccessibility()
        val params = GetAccessibilityPostRequest(
            placeId = place1.id
        )
        mvc
            .sccRequest("/getAccessibility", params, user = user)
            .andExpect {
                status {
                    isOk()
                }
            }
            .apply {
                val result = getResult(AccessibilityInfoDto::class)
                assertTrue(result.placeAccessibility!!.deletionInfo!!.isLastInBuilding)
            }

        mockSccClock.advanceTime(PlaceAccessibility.deletableDuration + Duration.ofMinutes(1))

        mvc
            .sccRequest("/getAccessibility", params, user = user)
            .andExpect {
                status {
                    isOk()
                }
            }
            .apply {
                val result = getResult(AccessibilityInfoDto::class)
                assertNull(result.placeAccessibility!!.deletionInfo)
            }
    }

    @Test
    fun `imageUrls 만 존재한다면 images 로 마이그레이션이 이뤄진다`() {
        val imageUrl = "https://example.com/image.jpg"
        val (user, place) = registerAccessibilityWithImages(imageUrls = listOf(imageUrl))
        val params = GetAccessibilityPostRequest(
            placeId = place.id
        )
        mvc
            .sccRequest("/getAccessibility", params, user = user)
            .andExpect {
                status {
                    isOk()
                }
            }
            .apply {
                val result = getResult(AccessibilityInfoDto::class)
                assertTrue(result.placeAccessibility!!.imageUrls.contains(imageUrl))
                assertFalse(result.placeAccessibility!!.images.isNullOrEmpty())
                assertEquals(imageUrl, result.placeAccessibility!!.images!!.first().imageUrl)
            }
    }

    @Test
    fun `썸네일이 존재하면 함께 내려준다`() {
        val imageUrl = "https://example.com/image.jpg"
        val thumbnailUrl = "https://example.com/thumbnail.jpg"
        // 하위 호환성
        val imageUrls = listOf(imageUrl)
        val images = listOf(AccessibilityImage(imageUrl, thumbnailUrl))

        val (user, place) = registerAccessibilityWithImages(imageUrls = imageUrls, images = images)
        val params = GetAccessibilityPostRequest(
            placeId = place.id
        )

        mvc
            .sccRequest("/getAccessibility", params, user = user)
            .andExpect {
                status {
                    isOk()
                }
            }
            .apply {
                val result = getResult(AccessibilityInfoDto::class)
                assertTrue(result.placeAccessibility!!.imageUrls.contains(imageUrl))
                assertFalse(result.placeAccessibility!!.images.isNullOrEmpty())
                assertEquals(imageUrl, result.placeAccessibility!!.images!!.first().imageUrl)
                assertEquals(thumbnailUrl, result.placeAccessibility!!.images!!.first().thumbnailUrl)
            }
    }

    @Test
    fun `썸네일이 없으면 생성해서 내려준다`() {
        val imageUrl = "resources/example.jpg"
        // MockFileManagementService 에서 filename 을 그대로 return 하도록 했기 때문
        val expectedThumbnailUrl = "thumbnail_example.webp"
        val (user, place) = registerAccessibilityWithImages(imageUrls = listOf(imageUrl))
        val params = GetAccessibilityPostRequest(
            placeId = place.id
        )

        mvc
            .sccRequest("/getAccessibility", params, user = user)
            .andExpect {
                status {
                    isOk()
                }
            }
            .apply {
                val result = getResult(AccessibilityInfoDto::class)
                assertTrue(result.placeAccessibility!!.imageUrls.contains(imageUrl))
                assertFalse(result.placeAccessibility!!.images.isNullOrEmpty())
                assertEquals(imageUrl, result.placeAccessibility!!.images!!.first().imageUrl)
                assertNotNull(result.placeAccessibility!!.images!!.first().thumbnailUrl)
                assertEquals(expectedThumbnailUrl, result.placeAccessibility!!.images!!.first().thumbnailUrl)
            }
    }

    @Test
    fun `즐겨찾기 등록한 유저에게는 즐겨찾기 등록했는지 정보를 내려준다`() {
        val (hasFavoriteUser, place) = registerAccessibility()
        transactionManager.doInTransaction { testDataGenerator.createPlaceFavorite(hasFavoriteUser.id, place.id) }
        mvc
            .sccRequest("/getAccessibility", GetAccessibilityPostRequest(placeId = place.id), user = hasFavoriteUser)
            .andExpect {
                status {
                    isOk()
                }
            }
            .apply {
                val result = getResult(AccessibilityInfoDto::class)
                assertTrue(result.isFavoritePlace)
                assertEquals(result.totalFavoriteCount, 1L)
            }

        val doesNotHaveFavoriteUser = testDataGenerator.createUser()
        mvc
            .sccRequest(
                "/getAccessibility",
                GetAccessibilityPostRequest(placeId = place.id),
                user = doesNotHaveFavoriteUser
            )
            .andExpect {
                status { isOk() }
            }
            .apply {
                val result = getResult(AccessibilityInfoDto::class)
                assertFalse(result.isFavoritePlace)
                assertEquals(result.totalFavoriteCount, 1L)
            }
    }

    @Test
    fun `현재 사용중인 s3 bucket 에 대해서만 CDN 으로 라우팅된 이미지 url 이 내려간다`() {
        val imageUrlFromActiveBucket = "https://test.s3.ap-northeast-2.amazonaws.com/1.jpg"
        val imageUrlFromOldBucket = "https://some-other-bucket.s3.ap-northeast-2.amazonaws.com/2.jpg"
        val cdnDomain = "https://cloudfronttest"

        val (user, place1) = registerAccessibilityWithImages(listOf(imageUrlFromActiveBucket, imageUrlFromOldBucket))
        val params = GetAccessibilityPostRequest(
            placeId = place1.id
        )
        mvc
            .sccRequest("/getAccessibility", params, user = user)
            .andExpect {
                status {
                    isOk()
                }
            }
            .apply {
                val result = getResult(AccessibilityInfoDto::class)
                assertEquals(2, result.placeAccessibility!!.imageUrls.size)
                assertNotEquals(imageUrlFromActiveBucket, result.placeAccessibility!!.imageUrls[0])
                assertTrue(result.placeAccessibility!!.imageUrls[0].startsWith(cdnDomain))
                assertEquals(imageUrlFromOldBucket, result.placeAccessibility!!.imageUrls[1])
            }
    }

    private fun registerAccessibility(overridingBuilding: Building? = null): RegisterAccessibilityResult {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createUser()
        }
        return transactionManager.doInTransaction {
            val place = testDataGenerator.createBuildingAndPlace(placeName = "장소장소", building = overridingBuilding)
            val (placeAccessibility, buildingAccessibility) = testDataGenerator.registerBuildingAndPlaceAccessibility(
                place,
                user
            )

            repeat(2) {
                testDataGenerator.giveBuildingAccessibilityUpvote(buildingAccessibility)
            }
            testDataGenerator.giveBuildingAccessibilityUpvote(buildingAccessibility, user)

            val buildingAccessibilityComment =
                testDataGenerator.registerBuildingAccessibilityComment(place.building, "건물 코멘트")
            val placeAccessibilityComment = testDataGenerator.registerPlaceAccessibilityComment(place, "장소 코멘트", user)

            RegisterAccessibilityResult(
                user,
                place,
                placeAccessibility,
                buildingAccessibility,
                placeAccessibilityComment,
                buildingAccessibilityComment
            )
        }
    }

    private fun registerAccessibilityWithImages(
        imageUrls: List<String>? = null,
        images: List<AccessibilityImage>? = null
    ): RegisterAccessibilityResult {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createUser()
        }
        return transactionManager.doInTransaction {
            val place = testDataGenerator.createBuildingAndPlace(placeName = "장소장소")
            val (placeAccessibility, buildingAccessibility) = testDataGenerator.registerBuildingAndPlaceAccessibility(
                place,
                user,
                imageUrls ?: emptyList(),
                images ?: emptyList()
            )

            val buildingAccessibilityComment =
                testDataGenerator.registerBuildingAccessibilityComment(place.building, "건물 코멘트")
            val placeAccessibilityComment = testDataGenerator.registerPlaceAccessibilityComment(place, "장소 코멘트", user)

            RegisterAccessibilityResult(
                user,
                place,
                placeAccessibility,
                buildingAccessibility,
                placeAccessibilityComment,
                buildingAccessibilityComment
            )
        }
    }

    private data class RegisterAccessibilityResult(
        val user: User,
        val place: Place,
        val placeAccessibility: PlaceAccessibility,
        val buildingAccessibility: BuildingAccessibility,
        val placeAccessibilityComment: PlaceAccessibilityComment,
        val buildingAccessibilityComment: BuildingAccessibilityComment,
    )
}
