package club.staircrusher.challenge.application.port.out.persistence

import club.staircrusher.challenge.domain.model.ChallengeRank
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface ChallengeRankRepository : CrudRepository<ChallengeRank, String> {
    @Query("""
        SELECT r
        FROM ChallengeRank r
        WHERE r.challengeId = :challengeId
        ORDER BY r.rank ASC
        LIMIT :n
    """)
    fun findTopNUsers(challengeId: String, n: Int): List<ChallengeRank>
    fun findFirstByChallengeIdAndUserId(challengeId: String, userId: String): ChallengeRank?
    @Query("""
        SELECT r
        FROM ChallengeRank r
        WHERE
            r.challengeId = :challengeId AND
            r.rank < :rank
        ORDER BY r.rank DESC
        LIMIT 1
    """)
    fun findNextRank(challengeId: String, rank: Long): ChallengeRank?

    @Query("""
        DELETE
        FROM ChallengeRank r
        WHERE r.challengeId = :challengeId
    """)
    @Modifying
    fun deleteByChallengeId(challengeId: String)
}
