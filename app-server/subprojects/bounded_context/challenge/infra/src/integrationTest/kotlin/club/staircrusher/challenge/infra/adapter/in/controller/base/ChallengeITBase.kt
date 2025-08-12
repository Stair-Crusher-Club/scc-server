package club.staircrusher.challenge.infra.adapter.`in`.controller.base

import club.staircrusher.challenge.domain.model.Challenge
import club.staircrusher.challenge.domain.model.ChallengeContribution
import club.staircrusher.challenge.domain.model.ChallengeParticipation
import club.staircrusher.challenge.domain.model.ChallengeStatus
import club.staircrusher.place.domain.model.place.Place
import club.staircrusher.testing.spring_it.base.SccSpringITBase
import club.staircrusher.user.domain.model.UserAccount
import java.time.Duration
import java.time.Instant
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.random.nextLong

open class ChallengeITBase : SccSpringITBase() {

    /**
     * @return 생성된 챌린지
     */
    fun registerChallenges(): Map<ChallengeStatus, List<Challenge>> {
        // 진행 중
        val inProgressChallenges = mutableListOf<Challenge>()
        repeat(Random.nextInt(3 until 10)) {
            inProgressChallenges += registerInProgressChallenge()
        }
        inProgressChallenges += registerInProgressChallenge(isInfiniteChallenge = true)

        // 오픈 예정
        val upcomingChallenges = mutableListOf<Challenge>()
        repeat(Random.nextInt(3 until 10)) {
            upcomingChallenges += registerUpcomingChallenge()
        }

        // 종료된
        val closedChallenges = mutableListOf<Challenge>()
        repeat(Random.nextInt(3 until 10)) {
            closedChallenges += registerClosedChallenge()
        }

        return listOf(
            ChallengeStatus.IN_PROGRESS to inProgressChallenges,
            ChallengeStatus.UPCOMING to upcomingChallenges,
            ChallengeStatus.CLOSED to closedChallenges,
        ).toMap()
    }


    fun registerInProgressChallenge(
        goal: Int = 1000,
        passcode: String? = null,
        invitationCode: String? = null,
        companyName: String? = null,
        isInfiniteChallenge: Boolean = false
    ): Challenge {
        return transactionManager.doInTransaction {
            testDataGenerator.createChallenge(
                name = "진행중 챌린지",
                passcode = passcode,
                invitationCode = invitationCode,
                companyName = companyName,
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
        userAccount: UserAccount,
        challenge: Challenge,
        participateAt: Instant = clock.instant()
    ): ChallengeParticipation {
        return transactionManager.doInTransaction {
            testDataGenerator.participateChallenge(userAccount, challenge, participateAt)
        }
    }

    fun contributePlaceAccessibility(
        userAccount: UserAccount,
        challenge: Challenge,
        overridingPlace: Place? = null,
        contributeAt: Instant = clock.instant()
    ): ChallengeContribution {
        return transactionManager.doInTransaction {
            val place = overridingPlace ?: testDataGenerator.createBuildingAndPlace(placeName = "장소장소", building = null)
            val (placeAccessibility, _) = testDataGenerator.registerBuildingAndPlaceAccessibility(place, userAccount)
            testDataGenerator.contributeToChallenge(
                userAccount = userAccount,
                challenge = challenge,
                placeAccessibility = placeAccessibility,
                contributeAt = contributeAt
            )
        }
    }
}
