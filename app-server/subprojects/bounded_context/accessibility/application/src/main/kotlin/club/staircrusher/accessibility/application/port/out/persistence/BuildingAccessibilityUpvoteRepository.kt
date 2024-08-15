package club.staircrusher.accessibility.application.port.out.persistence

import club.staircrusher.accessibility.domain.model.BuildingAccessibilityUpvote
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface BuildingAccessibilityUpvoteRepository : CrudRepository<BuildingAccessibilityUpvote, String> {
    @Query("""
        SELECT bau
        FROM BuildingAccessibilityUpvote bau
        WHERE
            bau.userId = :userId
            AND bau.buildingAccessibilityId = :buildingAccessibilityId
            AND bau.deletedAt IS NULL
    """)
    fun findExistingUpvote(userId: String, buildingAccessibilityId: String): BuildingAccessibilityUpvote?
    fun countByBuildingAccessibilityId(buildingAccessibilityId: String): Int
}
