package club.staircrusher.challenge.application.port.out.persistence

import club.staircrusher.challenge.domain.model.Challenge
import club.staircrusher.stdlib.domain.repository.EntityRepository
import java.time.Instant

interface ChallengeRepository : EntityRepository<Challenge, String> {
    fun findAllOrderByCreatedAtDesc(): List<Challenge>
    fun findByIds(challengeIds: Collection<String>): List<Challenge>
    fun findByInvitationCode(invitationCode: String): Challenge?
    fun findByTime(
        startsAtRange: ClosedRange<Instant>,
        endsAtRange: ClosedRange<Instant>,
    ): List<Challenge>

    fun findByUidAndTime(
        userId: String,
        startsAtRange: ClosedRange<Instant>,
        endsAtRange: ClosedRange<Instant>,
    ): List<Challenge>

    fun notJoinedChallenges(
        userId: String,
        startsAtRange: ClosedRange<Instant>,
        endsAtRange: ClosedRange<Instant>,
    ): List<Challenge>

    fun remove(id: String)
}
