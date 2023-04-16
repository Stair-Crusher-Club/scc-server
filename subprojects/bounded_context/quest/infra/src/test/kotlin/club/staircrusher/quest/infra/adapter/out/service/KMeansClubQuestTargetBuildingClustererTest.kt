package club.staircrusher.quest.infra.adapter.out.service

import club.staircrusher.quest.domain.model.ClubQuestTargetBuilding
import club.staircrusher.stdlib.geography.Location
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class KMeansClubQuestTargetBuildingClustererTest {
    @Test
    fun test() {
        val locations = listOf(
            ClubQuestTargetBuilding(buildingId = "장소 1" , name = "장소 1", location = Location(lng = 1.0, lat = 1.0), places = emptyList()),
            ClubQuestTargetBuilding(buildingId = "장소 2" , name = "장소 2", location = Location(lng = 1.1, lat = 1.1), places = emptyList()),
            ClubQuestTargetBuilding(buildingId = "장소 3" , name = "장소 3", location = Location(lng = 2.0, lat = 2.0), places = emptyList()),
            ClubQuestTargetBuilding(buildingId = "장소 4" , name = "장소 4", location = Location(lng = 2.1, lat = 2.1), places = emptyList()),
        )
        repeat(100) {
            val result = KMeansClubQuestTargetBuildingClusterer().clusterBuildings(locations, 2)
            val sortedResult = result.toList().sortedBy { it.first.lng }
            when (sortedResult.size) {
                1 -> {
                    assertEquals(1.55, sortedResult[0].first.lng, 0.00001)
                    assertEquals(1.55, sortedResult[0].first.lat, 0.00001)
                }
                2 -> {
                    assertEquals(1.05, sortedResult[0].first.lng, 0.00001)
                    assertEquals(1.05, sortedResult[0].first.lat, 0.00001)
                    assertEquals(2.05, sortedResult[1].first.lng, 0.00001)
                    assertEquals(2.05, sortedResult[1].first.lat, 0.00001)
                }
                else -> error("Should not reach here!")
            }
        }
    }
}
