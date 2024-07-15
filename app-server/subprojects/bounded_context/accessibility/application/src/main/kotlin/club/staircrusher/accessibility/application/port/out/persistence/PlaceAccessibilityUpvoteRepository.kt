package club.staircrusher.accessibility.application.port.out.persistence

import club.staircrusher.accessibility.domain.model.PlaceAccessibilityUpvote
import club.staircrusher.stdlib.domain.repository.EntityRepository

interface PlaceAccessibilityUpvoteRepository : EntityRepository<PlaceAccessibilityUpvote, String> {
    fun findUpvote(userId: String, placeAccessibilityId: String): PlaceAccessibilityUpvote?
    fun countUpvotes(placeAccessibilityId: String): Int
}
