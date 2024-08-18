package club.staircrusher.accessibility.application.port.out.persistence

import club.staircrusher.accessibility.domain.model.PlaceAccessibilityUpvote
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface PlaceAccessibilityUpvoteRepository : CrudRepository<PlaceAccessibilityUpvote, String> {
    @Query("""
        SELECT pau
        FROM PlaceAccessibilityUpvote pau
        WHERE
            pau.userId = :userId
            AND pau.placeAccessibilityId = :placeAccessibilityId
            AND pau.deletedAt IS NULL
    """)
    fun findExistingUpvote(userId: String, placeAccessibilityId: String): PlaceAccessibilityUpvote?
}
