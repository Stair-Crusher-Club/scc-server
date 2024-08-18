package club.staircrusher.challenge.application.port.out.persistence

import club.staircrusher.challenge.domain.model.Challenge
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.Instant

interface ChallengeRepository : CrudRepository<Challenge, String> {
    fun findAllByOrderByCreatedAtDesc(): List<Challenge>
    fun findFirstByInvitationCode(invitationCode: String): Challenge?
    @Query("""
        SELECT c
        FROM Challenge c
        WHERE
            :startsAtFrom < c.startsAt
            AND c.startsAt < :startsAtTo
            AND (
                c.endsAt IS NULL
                OR (
                    :endsAtFrom < c.endsAt
                    AND c.endsAt < :endsAtTo
                )
            )
        ORDER BY
            c.endsAt DESC
    """)
    fun findByTime(
        startsAtFrom: Instant,
        startsAtTo: Instant,
        endsAtFrom: Instant,
        endsAtTo: Instant,
    ): List<Challenge>

    @Query("""
        SELECT challenge.*
        FROM challenge
            JOIN (
                SELECT *
                FROM challenge_participation
                WHERE challenge_participation.user_id = :userId
            ) AS my_participations
            ON challenge.id = my_participations.challenge_id
        WHERE
            :startsAtFrom < challenge.starts_at
            AND challenge.starts_at < :startsAtTo
            AND (
                challenge.ends_at IS NULL
                OR (
                    :endsAtFrom < challenge.ends_at
                    AND challenge.ends_at < :endsAtTo
                )
            )
        ORDER BY my_participations.created_at DESC
    """, nativeQuery = true)
    fun findByUidAndTime(
        userId: String,
        startsAtFrom: Instant,
        startsAtTo: Instant,
        endsAtFrom: Instant,
        endsAtTo: Instant,
    ): List<Challenge>
}
