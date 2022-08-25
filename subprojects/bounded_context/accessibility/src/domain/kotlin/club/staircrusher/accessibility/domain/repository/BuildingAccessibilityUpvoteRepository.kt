package club.staircrusher.accessibility.domain.repository

import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.BuildingAccessibilityUpvote
import club.staircrusher.stdlib.domain.repository.EntityRepository

interface BuildingAccessibilityUpvoteRepository : EntityRepository<BuildingAccessibilityUpvote, String> {
    fun findByUserAndBuildingAccessibilityAndNotDeleted(userId: String, buildingAccessibility: BuildingAccessibility): BuildingAccessibilityUpvote?
    fun getTotalUpvoteCount(userId: String): Int
    fun getTotalUpvoteCount(buildingAccessibility: BuildingAccessibility): Int
}
