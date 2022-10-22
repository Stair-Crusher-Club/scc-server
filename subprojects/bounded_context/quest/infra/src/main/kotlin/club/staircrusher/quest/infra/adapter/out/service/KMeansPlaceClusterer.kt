package club.staircrusher.quest.infra.adapter.out.service

import club.staircrusher.quest.application.port.out.web.PlaceClusterer
import club.staircrusher.quest.domain.model.ClubQuestTargetPlace
import club.staircrusher.quest.infra.kmeans.EuclideanDistance
import club.staircrusher.quest.infra.kmeans.KMeans
import club.staircrusher.quest.infra.kmeans.Record
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.di.annotation.Component

@Component
class KMeansPlaceClusterer : PlaceClusterer {
    companion object {
        private const val REPEAT_NUM = 100
        private const val MAX_ITERATION = 1000
    }
    override fun clusterPlaces(places: List<ClubQuestTargetPlace>, clusterCount: Int): Map<Location, List<ClubQuestTargetPlace>> {
        val placesById = places.associateBy { it.placeId }
        val records = places.map {
            Record(
                it.placeId,
                mapOf(
                    "lng" to it.location.lng,
                    "lat" to it.location.lat,
                )
            )
        }
        repeat(REPEAT_NUM) { _ ->
            val result = KMeans.fit(records, clusterCount, EuclideanDistance(), MAX_ITERATION)
            // k-means를 돌리는 와중에 cluster count가 감소할 수 있는 것으로 보인다.
            // 그래서 cluster count가 감소되지 않았는지 확인하고, 감소되었으면 재시도한다.
            if (result.size == clusterCount) {
                return result.map { (centroid, records) ->
                    val clusterCenterLocation = Location(
                        lng = centroid.coordinates["lng"]!!,
                        lat = centroid.coordinates["lat"]!!,
                    )
                    val belongingPlaces = records.map { placesById[it.description]!! }
                    clusterCenterLocation to belongingPlaces
                }.toMap()
            }

        }
        throw error("Failed to clustering places. Please try again.")
    }
}
