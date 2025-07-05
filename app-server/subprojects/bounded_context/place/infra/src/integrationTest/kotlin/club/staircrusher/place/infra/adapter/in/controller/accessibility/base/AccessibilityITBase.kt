package club.staircrusher.place.infra.adapter.`in`.controller.accessibility.base

import club.staircrusher.place.domain.model.accessibility.BuildingAccessibility
import club.staircrusher.place.domain.model.accessibility.PlaceAccessibility
import club.staircrusher.place.domain.model.place.Building
import club.staircrusher.place.domain.model.place.Place
import club.staircrusher.testing.spring_it.base.SccSpringITBase
import club.staircrusher.user.domain.model.UserAccount

class AccessibilityITBase : SccSpringITBase() {
    fun registerAccessibility(overridingUser: UserAccount? = null, overridingBuilding: Building? = null): RegisterAccessibilityResult {
        val user = overridingUser ?: transactionManager.doInTransaction {
            testDataGenerator.createIdentifiedUser().account
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
        val user: UserAccount,
        val place: Place,
        val placeAccessibility: PlaceAccessibility,
        val buildingAccessibility: BuildingAccessibility,
    )
}
