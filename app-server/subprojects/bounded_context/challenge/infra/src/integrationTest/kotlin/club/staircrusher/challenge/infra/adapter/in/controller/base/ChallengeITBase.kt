package club.staircrusher.challenge.infra.adapter.`in`.controller.base

import club.staircrusher.challenge.domain.model.Challenge
import club.staircrusher.challenge.domain.model.ChallengeCondition
import club.staircrusher.challenge.domain.model.ChallengeContribution
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
        registerInProgressChallenge(isInfiniteChallenge = true)
        // 오픈 예정
        repeat(Random.nextInt(3 until 10)) {
            registerUpcomingChallenge()
        }
        // 종료된
        repeat(Random.nextInt(3 until 10)) {
            registerClosedChallenge()
        }
    }


    fun registerInProgressChallenge(
        goal: Int = 1000,
        passcode: String? = null,
        invitationCode: String? = null,
        isInfiniteChallenge: Boolean = false
    ): Challenge {
        return transactionManager.doInTransaction {
            testDataGenerator.createChallenge(
                name = "진행중 챌린지",
                passcode = passcode,
                invitationCode = invitationCode,
                goal = goal,
                startsAt = clock.instant().minus(Duration.ofHours(Random.nextLong(from = 1, until = 360))),
                endsAt = if (isInfiniteChallenge) null
                else clock.instant().plus(Duration.ofHours(Random.nextLong(from = 1, until = 360))),
                conditions = listOf()
            )
        }
    }

    fun registerUpcomingChallenge(passcode: String? = null): Challenge {
        return transactionManager.doInTransaction {
            val startsAt = clock.instant()
                .plus(Duration.ofHours(Random.nextLong(1L until 360L)))
            testDataGenerator.createChallenge(
                name = "오픈예정 챌린지",
                passcode = passcode,
                startsAt = startsAt,
                endsAt = startsAt
                    .plus(Duration.ofHours(Random.nextLong(1L until 360L))),
                conditions = listOf()
            )
        }
    }

    fun registerClosedChallenge(passcode: String? = null): Challenge {
        return transactionManager.doInTransaction {
            val endsAt = clock.instant()
                .minus(Duration.ofHours(Random.nextLong(1L until 360L)))
            testDataGenerator.createChallenge(
                name = "종료된 챌린지",
                passcode = passcode,
                startsAt = endsAt
                    .minus(Duration.ofHours(Random.nextLong(1L until 360L))),
                endsAt = endsAt,
                conditions = listOf()
            )
        }
    }

    fun participate(
        user: User,
        challenge: Challenge,
        participateAt: Instant = clock.instant()
    ): ChallengeParticipation {
        return transactionManager.doInTransaction {
            testDataGenerator.participateChallenge(user, challenge, participateAt)
        }
    }

    fun contributePlaceAccessibility(
        user: User,
        challenge: Challenge,
        contributeAt: Instant = clock.instant()
    ): ChallengeContribution {
        return transactionManager.doInTransaction {
            val place = testDataGenerator.createBuildingAndPlace(placeName = "장소장소", building = null)
            val (placeAccessibility, _) = testDataGenerator.registerBuildingAndPlaceAccessibility(place, user)
            testDataGenerator.contributeChallenge(
                user = user,
                challenge = challenge,
                placeAccessibility = placeAccessibility,
                contributeAt = contributeAt
            )
        }
    }
}
