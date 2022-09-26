package club.staircrusher.accessibility.domain.repository

import club.staircrusher.accessibility.domain.model.BuildingAccessibilityComment
import club.staircrusher.stdlib.domain.repository.EntityRepository


interface BuildingAccessibilityCommentRepository : EntityRepository<BuildingAccessibilityComment, String> {
    fun findByBuildingId(buildingId: String): List<BuildingAccessibilityComment>
}
