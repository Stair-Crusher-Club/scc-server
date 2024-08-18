package club.staircrusher.accessibility.application.port.out.persistence

import club.staircrusher.accessibility.domain.model.AccessibilityRank
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface AccessibilityRankRepository: CrudRepository<AccessibilityRank, String> {
    @Query("""
        SELECT r
        FROM AccessibilityRank r
        ORDER BY r.rank ASC
        LIMIT :n
    """)
    fun findTopNUsers(n: Int): List<AccessibilityRank>
    fun findFirstByUserId(userId: String): AccessibilityRank?
    @Query("""
        SELECT r
        FROM AccessibilityRank r
        WHERE r.rank < :rank
        ORDER BY r.rank DESC
        LIMIT 1
    """)
    fun findNextRank(rank: Long): AccessibilityRank?
    @Query("""
        SELECT rank
        FROM accessibility_rank r
        WHERE conquered_count = :conqueredCount
        LIMIT 1
    """, nativeQuery = true)
    fun findRankByConqueredCount(conqueredCount: Int): Long?
}
