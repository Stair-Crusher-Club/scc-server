package club.staircrusher.challenge.infra.adapter.`in`.controller.base

import club.staircrusher.challenge.domain.model.Challenge
import club.staircrusher.challenge.domain.model.ChallengeCondition
import club.staircrusher.challenge.domain.model.ChallengeParticipation
import club.staircrusher.testing.spring_it.base.SccSpringITBase
import club.staircrusher.user.domain.model.User
import org.springframework.beans.factory.annotation.Autowired
import java.time.Clock
import java.time.Duration
import java.time.Instant
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.random.nextLong

open class ChallengeITBase : SccSpringITBase() {

    @Autowired
    private lateinit var clock: Clock

    fun registerChallenges() {
        // 진행 중
        repeat(Random.nextInt(3 until 10)) {
            registerInProgressChallenge()
        }
        // 오픈 예정
        repeat(Random.nextInt(3 until 10)) {
            registerUpcomingChallenge()
        }
        // 종료된
        repeat(Random.nextInt(3 until 10)) {
            registerClosedChallenge()
        }
    }


    fun registerInProgressChallenge(passcode: String? = null): Challenge {
        return transactionManager.doInTransaction {
            testDataGenerator.createChallenge(
                name = "",
                passcode = passcode,
                startsAt = clock.instant().minus(Duration.ofHours(Random.nextLong(from = 1, until = 360))),
                endsAt = clock.instant().plus(Duration.ofHours(Random.nextLong(from = 1, until = 360))),
                conditions = listOf(
                    ChallengeCondition(
                        addressMatches = listOf(),
                        accessibilityTypes = listOf()
                    )
                )
            )
        }
    }

    fun registerUpcomingChallenge(passcode: String? = null): Challenge {
        return transactionManager.doInTransaction {
            val startsAt = clock.instant()
                .plus(Duration.ofHours(Random.nextLong(1L until 360L)))
            testDataGenerator.createChallenge(
                name = "",
                passcode = passcode,
                startsAt = startsAt,
                endsAt = startsAt
                    .plus(Duration.ofHours(Random.nextLong(1L until 360L))),
                conditions = listOf(
                    ChallengeCondition(
                        addressMatches = listOf(),
                        accessibilityTypes = listOf()
                    )
                )
            )
        }
    }


    fun registerClosedChallenge(passcode: String? = null): Challenge {
        return transactionManager.doInTransaction {
            val endsAt = clock.instant()
                .minus(Duration.ofHours(Random.nextLong(1L until 360L)))
            testDataGenerator.createChallenge(
                name = "",
                passcode = passcode,
                startsAt = endsAt
                    .minus(Duration.ofHours(Random.nextLong(1L until 360L))),
                endsAt = endsAt,
                conditions = listOf(
                    ChallengeCondition(
                        addressMatches = listOf(),
                        accessibilityTypes = listOf()
                    )
                )
            )
        }
    }

    fun participate(user: User, challenge: Challenge, participateAt: Instant = clock.instant()): ChallengeParticipation {
        return transactionManager.doInTransaction {
            testDataGenerator.participateChallenge(user, challenge, participateAt)
        }
    }
}
