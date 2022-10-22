package club.staircrusher.accessibility.application.port.out.persistence

import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.BuildingAccessibilityUpvote
import club.staircrusher.stdlib.domain.repository.EntityRepository

interface BuildingAccessibilityUpvoteRepository : EntityRepository<BuildingAccessibilityUpvote, String> {
    fun findByUserAndBuildingAccessibilityAndNotDeleted(userId: String, buildingAccessibility: BuildingAccessibility): BuildingAccessibilityUpvote?
    fun getTotalUpvoteCount(userId: String): Int
}
