package club.staircrusher.quest.domain.service

import club.staircrusher.stdlib.geography.Location

interface PlaceClusterer {
    fun clusterPlaces(locations: List<Location>, clusterCount: Int): Map<Location, List<Location>>
}
