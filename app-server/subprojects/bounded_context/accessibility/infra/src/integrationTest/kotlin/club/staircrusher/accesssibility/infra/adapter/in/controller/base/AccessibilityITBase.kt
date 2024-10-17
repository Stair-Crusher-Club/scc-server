package club.staircrusher.accesssibility.infra.adapter.`in`.controller.base

import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.place.domain.model.Building
import club.staircrusher.place.domain.model.Place
import club.staircrusher.testing.spring_it.base.SccSpringITBase
import club.staircrusher.user.domain.model.User

class AccessibilityITBase : SccSpringITBase() {
    fun registerAccessibility(overridingUser: User? = null, overridingBuilding: Building? = null): RegisterAccessibilityResult {
        val user = overridingUser ?: transactionManager.doInTransaction {
            testDataGenerator.createUser()
        }
        return transactionManager.doInTransaction {
            val place = testDataGenerator.createBuildingAndPlace(placeName = "장소장소", building = overridingBuilding)
            val (placeAccessibility, buildingAccessibility) = testDataGenerator.registerBuildingAndPlaceAccessibility(place, user)

            testDataGenerator.registerBuildingAccessibilityComment(place.building, "건물 코멘트")
            testDataGenerator.registerPlaceAccessibilityComment(place, "장소 코멘트", user)

            RegisterAccessibilityResult(user, place, placeAccessibility, buildingAccessibility)
        }
    }

    data class RegisterAccessibilityResult(
        val user: User,
        val place: Place,
        val placeAccessibility: PlaceAccessibility,
        val buildingAccessibility: BuildingAccessibility,
    )
}
