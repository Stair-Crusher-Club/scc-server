package club.staircrusher.challenge.application.port.`in`

import club.staircrusher.challenge.application.port.out.persistence.ChallengeParticipationRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeRepository
import club.staircrusher.challenge.domain.model.Challenge
import club.staircrusher.stdlib.di.annotation.Component
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
}
