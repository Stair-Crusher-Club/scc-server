package club.staircrusher.accessibility.application.port.out.persistence

import club.staircrusher.accessibility.domain.model.AccessibilityRank
import club.staircrusher.stdlib.domain.repository.EntityRepository

interface AccessibilityRankRepository: EntityRepository<AccessibilityRank, String> {
    fun findTopNUsers(n: Int): List<AccessibilityRank>
    fun findByUserId(userId: String): AccessibilityRank?
    fun findByRank(rank: Long): AccessibilityRank?
    fun findByConqueredCount(conqueredCount: Int): AccessibilityRank?
    fun findAll(): List<AccessibilityRank>
}
