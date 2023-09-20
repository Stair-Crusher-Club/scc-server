package club.staircrusher.challenge.application.port.`in`

import club.staircrusher.challenge.application.port.out.persistence.ChallengeParticipationRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeRepository
import club.staircrusher.challenge.domain.model.Challenge
import club.staircrusher.challenge.domain.model.ChallengeParticipation
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.persistence.TransactionManager
import java.time.Clock
import java.time.Duration
import java.time.Instant

@Component
class ChallengeService(
    private val transactionManager: TransactionManager,
    private val challengeRepository: ChallengeRepository,
    private val challengeParticipationRepository: ChallengeParticipationRepository,
    private val clock: Clock,
) {
    companion object {
        // Instant.MAX 는 범위 초과로 1000년을 추가해서 쓴다.
        val MAX = Instant.EPOCH.plus(Duration.ofDays(365 * 1000))
        val MIN = Instant.EPOCH
    }

    sealed class MyChallengeOption {
        data class Only(val userId: String) : MyChallengeOption()
        data class Without(val userId: String) : MyChallengeOption()
    }

    fun getInProgressChallenges(option: MyChallengeOption? = null): List<Challenge> {
        return transactionManager.doInTransaction {
            return@doInTransaction when (option) {
                is MyChallengeOption.Only ->
                    challengeRepository.joinedChallenges(
                        userId = option.userId,
                        startsAtRange = MIN.rangeTo(clock.instant()),
                        endsAtRange = clock.instant().rangeTo(MAX),
                    )

                is MyChallengeOption.Without ->
                    challengeRepository.notJoinedChallenges(
                        userId = option.userId,
                        startsAtRange = MIN.rangeTo(clock.instant()),
                        endsAtRange = clock.instant().rangeTo(MAX),
                    )

                null -> challengeRepository.findByTime(
                    startsAtRange = MIN.rangeTo(clock.instant()),
                    endsAtRange = clock.instant().rangeTo(MAX),
                )
            }
        }
    }

    fun getUpcomingChallenges(): List<Challenge> {
        return transactionManager.doInTransaction {
            return@doInTransaction challengeRepository.findByTime(
                startsAtRange = clock.instant().rangeTo(MAX),
                endsAtRange = clock.instant().rangeTo(MAX),
            )
        }
    }

    fun getClosedChallenges(): List<Challenge> {
        return transactionManager.doInTransaction {
            return@doInTransaction challengeRepository.findByTime(
                startsAtRange = MIN.rangeTo(clock.instant()),
                endsAtRange = MIN.rangeTo(clock.instant()),
            )
        }
    }

    @Suppress("ThrowsCount")
    fun joinChallenge(userId: String, challengeId: String, passcode: String?): Challenge {
        return transactionManager.doInTransaction {
            val alreadyJoined = challengeParticipationRepository.findByChallengeIdAndUserId(challengeId, userId) != null
            if (alreadyJoined) {
                throw SccDomainException(msg = "이미 참여한 챌린지입니다.", errorCode = SccDomainException.ErrorCode.ALREADY_JOINED)
            }
            val challenge = challengeRepository.findById(challengeId)
            if (challenge.passcode != null && challenge.passcode != passcode) {
                throw SccDomainException(msg = "잘못된 참여코드 입니다.", errorCode = SccDomainException.ErrorCode.INVALID_PASSCODE)
            }
            val now = clock.instant()
            if (now < challenge.startsAt) {
                throw SccDomainException(msg = "아직 오픈 전 입니다.", errorCode = SccDomainException.ErrorCode.CHALLENGE_NOT_OPENED)
            }
            if (challenge.endsAt?.let { it < now } == true) {
                throw SccDomainException(msg = "이미 종료되었습니다.", errorCode = SccDomainException.ErrorCode.CHALLENGE_CLOSED)
            }
            challengeParticipationRepository.save(
                ChallengeParticipation(
                    id = EntityIdGenerator.generateRandom(),
                    challengeId = challenge.id,
                    userId = userId,
                    createdAt = clock.instant()
                )
            )
            return@doInTransaction challenge
        }
    }
}
