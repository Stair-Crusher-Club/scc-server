package club.staircrusher.accessibility.application.port.out.persistence

import club.staircrusher.accessibility.domain.model.AccessibilityRank
import club.staircrusher.stdlib.domain.repository.EntityRepository

interface AccessibilityRankRepository: EntityRepository<AccessibilityRankRepository, String> {
    data class CreateParams(
        val userId: String,
        val conquestCount: Int,
        val rank: Long?,
    )

    fun findTopNUsers(n: Int): List<AccessibilityRank>
    fun findByUserId(userId: String): AccessibilityRank?
    fun findByRank(rank: Long): AccessibilityRank?
}
