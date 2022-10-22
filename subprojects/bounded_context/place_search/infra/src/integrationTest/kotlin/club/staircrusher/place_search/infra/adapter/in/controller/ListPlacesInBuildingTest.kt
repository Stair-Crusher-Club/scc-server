package club.staircrusher.place_search.infra.adapter.`in`.controller

// TODO: kakao 지도 사용하도록 변경한 로직에 맞게 테스트 컨버팅
//import club.staircrusher.api.spec.dto.ListPlacesInBuildingPostRequest
//import club.staircrusher.api.spec.dto.PlaceListItem
//import club.staircrusher.place_search.infra.adapter.`in`.controller.base.PlaceSearchITBase
//import com.fasterxml.jackson.core.type.TypeReference
//import org.junit.jupiter.api.Assertions.assertEquals
//import org.junit.jupiter.api.Assertions.assertNull
//import org.junit.jupiter.api.Assertions.assertTrue
//import org.junit.jupiter.api.Test
//import kotlin.random.Random
//
//class ListPlacesInBuildingTest : PlaceSearchITBase() {
//    @Test
//    fun testListPlacesInBuilding() {
//        val user = transactionManager.doInTransaction {
//            testDataGenerator.createUser()
//        }
//        val placesCount = 10
//        val accessibilityRegisteredPlaceIds = mutableSetOf<String>()
//        val (building, places) = transactionManager.doInTransaction {
//            val building = testDataGenerator.createBuilding()
//            testDataGenerator.registerBuildingAccessibility(building)
//            val places = (1..placesCount).map {
//                val place = testDataGenerator.createPlace(placeName = Random.nextBytes(32).toString(), building = building)
//                if (it % 3 == 0) {
//                    testDataGenerator.registerPlaceAccessibility(place)
//                    accessibilityRegisteredPlaceIds.add(place.id)
//                }
//                place
//            }
//            Pair(building, places)
//        }
//
//        val params = ListPlacesInBuildingPostRequest(
//            buildingId = building.id
//        )
//        mvc
//            .sccRequest("/listPlacesInBuilding", params, user = user)
//            .apply {
//                val result = getResult(object : TypeReference<List<PlaceListItem>>() {})
//                assertEquals(placesCount, result.size)
//                result.forEach { item ->
//                    val place = places.find { it.id == item.place.id }!!
//                    assertEquals(building.id, item.building.id)
//                    assertEquals(place.id in accessibilityRegisteredPlaceIds, item.hasPlaceAccessibility)
//                    assertTrue(item.hasBuildingAccessibility)
//                    assertNull(item.distanceMeters)
//                }
//            }
//    }
//
//    // TODO: 유저 없는 경우도 테스트?
//}
