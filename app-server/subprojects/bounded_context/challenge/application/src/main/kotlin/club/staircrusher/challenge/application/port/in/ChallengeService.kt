package club.staircrusher.challenge.application.port.`in`

import club.staircrusher.challenge.application.port.out.persistence.ChallengeRepository
import club.staircrusher.challenge.domain.model.Challenge
import club.staircrusher.stdlib.di.annotation.Component
import java.time.Clock
import java.time.Instant

@Component
class ChallengeService(
    private val challengeRepository: ChallengeRepository,
    private val clock: Clock,
) {

    fun getMyInProgressChallenges(userId: String, criteriaTime: Instant = clock.instant()): List<Challenge> {
        return challengeRepository.joinedChallenges(
            userId = userId,
            startsAtRange = Challenge.MIN_TIME.rangeTo(criteriaTime),
            endsAtRange = criteriaTime.rangeTo(Challenge.MAX_TIME),
        )
    }

    fun getInProgressChallenges(criteriaTime: Instant = clock.instant()): List<Challenge> {
        return challengeRepository.findByTime(
            startsAtRange = Challenge.MIN_TIME.rangeTo(criteriaTime),
            endsAtRange = criteriaTime.rangeTo(Challenge.MAX_TIME),
        )
    }

    fun getUpcomingChallenges(criteriaTime: Instant = clock.instant()): List<Challenge> {
        return challengeRepository.findByTime(
            startsAtRange = criteriaTime.rangeTo(Challenge.MAX_TIME),
            endsAtRange = criteriaTime.rangeTo(Challenge.MAX_TIME),
        )
    }

    fun getClosedChallenges(criteriaTime: Instant = clock.instant()): List<Challenge> {
        return challengeRepository.findByTime(
            startsAtRange = Challenge.MIN_TIME.rangeTo(criteriaTime),
            endsAtRange = Challenge.MIN_TIME.rangeTo(criteriaTime),
        )
    }
}
