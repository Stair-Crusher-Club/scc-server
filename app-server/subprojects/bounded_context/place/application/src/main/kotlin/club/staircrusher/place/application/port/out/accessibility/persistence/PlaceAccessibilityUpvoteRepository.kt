package club.staircrusher.place.application.port.out.accessibility.persistence

import club.staircrusher.place.domain.model.accessibility.PlaceAccessibilityUpvote
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
