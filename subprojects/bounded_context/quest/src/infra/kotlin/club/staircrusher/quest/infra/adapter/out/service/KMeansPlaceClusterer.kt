package club.staircrusher.quest.infra.adapter.out.service

import club.staircrusher.quest.domain.service.PlaceClusterer
import club.staircrusher.quest.infra.kmeans.EuclideanDistance
import club.staircrusher.quest.infra.kmeans.KMeans
import club.staircrusher.quest.infra.kmeans.Record
import club.staircrusher.stdlib.geography.Location
import org.springframework.stereotype.Component

@Component
class KMeansPlaceClusterer : PlaceClusterer {
    override fun clusterPlaces(locations: List<Location>, clusterCount: Int): Map<Location, List<Location>> {
        val records = locations.map {
            Record(
                mapOf(
                    "lng" to it.lng,
                    "lat" to it.lat,
                )
            )
        }
        repeat(100) { iteration ->
            val result = KMeans.fit(records, clusterCount, EuclideanDistance(), 1000)
            // k-means를 돌리는 와중에 cluster count가 감소할 수 있는 것으로 보인다.
            // 그래서 cluster count가 감소되지 않았는지 확인하고, 감소되었으면 재시도한다.
            if (result.size == clusterCount) {
                return result.map { (centroid, records) ->
                    val clusterCenterLocation = Location(
                        lng = centroid.coordinates["lng"]!!,
                        lat = centroid.coordinates["lat"]!!,
                    )
                    val belongingLocations = records.map {
                        Location(
                            lng = it.features["lng"]!!,
                            lat = it.features["lat"]!!,
                        )
                    }
                    clusterCenterLocation to belongingLocations
                }.toMap()
            }

        }
        throw IllegalStateException("Failed to clustering places. Please try again.")
    }
}
