package club.staircrusher.quest.infra.adapter.out.service

import club.staircrusher.place.domain.model.Building
import club.staircrusher.quest.application.port.out.web.ClubQuestTargetBuildingClusterer
import club.staircrusher.quest.infra.kmeans.Centroid
import club.staircrusher.quest.infra.kmeans.EuclideanDistance
import club.staircrusher.quest.infra.kmeans.KMeans
import club.staircrusher.quest.infra.kmeans.Record
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.geography.Location

@Component
class KMeansClubQuestTargetBuildingClusterer : ClubQuestTargetBuildingClusterer {
    companion object {
        private const val REPEAT_NUM = 100
        private const val MAX_ITERATION = 1000
    }

    @Suppress("ReturnCount")
    override fun clusterBuildings(buildings: List<Building>, clusterCount: Int): Map<Location, List<Building>> {
        if (clusterCount <= 0) {
            return emptyMap()
        }

        if (buildings.size <= clusterCount) {
            return buildings.associate {
                it.location to listOf(it)
            }
        }

        val buildingById = buildings.associateBy { it.id }
        val records = buildings.map {
            Record(
                it.id,
                mapOf(
                    "lng" to it.location.lng,
                    "lat" to it.location.lat,
                )
            )
        }
        if (clusterCount == 1) {
            val centerLng = buildings.sumOf { it.location.lng } / buildings.count()
            val centerLat = buildings.sumOf { it.location.lat } / buildings.count()
            return mapOf(Location(centerLng, centerLat) to buildings)
        }
        var result: Map<Centroid, List<Record>>? = null
        repeat(REPEAT_NUM) { iteration ->
            result = KMeans.fit(records, clusterCount, EuclideanDistance(), MAX_ITERATION)
            // k-means를 돌리는 와중에 cluster count가 감소할 수 있는 것으로 보인다.
            // 그래서 cluster count가 감소되지 않았는지 확인하고, 감소되었으면 재시도한다.
            if (result!!.size == clusterCount) {
               return@repeat
            }
        }
        return result!!.map { (centroid, records) ->
            val clusterCenterLocation = Location(
                lng = centroid.coordinates["lng"]!!,
                lat = centroid.coordinates["lat"]!!,
            )
            val belongingPlaces = records.map { buildingById[it.description]!! }
            clusterCenterLocation to belongingPlaces
        }.toMap()
    }
}
