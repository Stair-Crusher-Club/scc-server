package club.staircrusher.accessibility.domain.service

import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.Place
import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.accessibility.domain.repository.BuildingAccessibilityRepository
import club.staircrusher.accessibility.domain.repository.PlaceAccessibilityRepository

class SearchAccessibilityService(
    private val placeAccessibilityRepository: PlaceAccessibilityRepository,
    private val buildingAccessibilityRepository: BuildingAccessibilityRepository,
) {
    data class Result(
        private val places: List<Place>,
        private val placeAccessibilities: List<PlaceAccessibility>,
        private val buildingAccessibilities: List<BuildingAccessibility>,
    ) {
        private val accessibilityByPlaceId = run {
            val placeAccessibilityByPlaceId = placeAccessibilities.associateBy { it.placeId }
            val buildingAccessibilityByBuildingId = buildingAccessibilities.associateBy { it.buildingId }
            places.map {
                it.id to Pair(placeAccessibilityByPlaceId[it.id], buildingAccessibilityByBuildingId[it.buildingId])
            }.toMap()
        }

        fun getAccessibility(placeId: String): Pair<PlaceAccessibility?, BuildingAccessibility?> {
            return accessibilityByPlaceId[placeId] ?: throw IllegalArgumentException("search() 메소드의 인자로 넣은 place가 아닙니다.")
        }
    }

    fun search(places: List<Place>): Result {
        val placeAccessibilities = placeAccessibilityRepository.findByPlaceIds(places.map { it.id })
        val buildingAccessibilities = buildingAccessibilityRepository.findByBuildingIds(places.map { it.buildingId })
        return Result(
            places = places,
            placeAccessibilities = placeAccessibilities,
            buildingAccessibilities = buildingAccessibilities,
        )
    }
}
