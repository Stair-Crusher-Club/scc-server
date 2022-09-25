package club.staircrusher.quest.infra.adapter.out.service

import club.staircrusher.quest.domain.vo.ClubQuestTargetPlace
import club.staircrusher.stdlib.geography.Location
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class KMeansPlaceClustererTest {
    @Test
    fun test() {
        val locations = listOf(
            ClubQuestTargetPlace(name = "장소 1", location = Location(lng = 1.0, lat = 1.0), placeId = "장소 1"),
            ClubQuestTargetPlace(name = "장소 2", location = Location(lng = 1.1, lat = 1.1), placeId = "장소 2"),
            ClubQuestTargetPlace(name = "장소 3", location = Location(lng = 2.0, lat = 2.0), placeId = "장소 3"),
            ClubQuestTargetPlace(name = "장소 4", location = Location(lng = 2.1, lat = 2.1), placeId = "장소 4"),
        )
        repeat(100) {
            val result = KMeansPlaceClusterer().clusterPlaces(locations, 2)
            val sortedResult = result.toList().sortedBy { it.first.lng }
            try {
                assertEquals(1.05, sortedResult[0].first.lng, 0.00001)
                assertEquals(1.05, sortedResult[0].first.lat, 0.00001)
                assertEquals(2.05, sortedResult[1].first.lng, 0.00001)
                assertEquals(2.05, sortedResult[1].first.lat, 0.00001)
            } catch (t: Throwable) {
                throw t
            }
        }
    }
}
