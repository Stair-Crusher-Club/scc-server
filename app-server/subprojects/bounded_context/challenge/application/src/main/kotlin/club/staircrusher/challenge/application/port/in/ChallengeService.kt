package club.staircrusher.challenge.application.port.`in`

import club.staircrusher.challenge.application.port.out.persistence.ChallengeContributionRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeParticipationRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeRepository
import club.staircrusher.challenge.domain.model.Challenge
import club.staircrusher.challenge.domain.model.ChallengeActionCondition
import club.staircrusher.challenge.domain.model.ChallengeAddress
import club.staircrusher.challenge.domain.model.ChallengeContribution
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.user.application.port.out.persistence.UserRepository
import java.time.Clock
import java.time.Instant

@Component
class ChallengeService(
    private val userRepository: UserRepository,
    private val challengeRepository: ChallengeRepository,
    private val challengeContributionRepository: ChallengeContributionRepository,
    private val challengeParticipationRepository: ChallengeParticipationRepository,
    private val clock: Clock,
) {
    sealed class Contribution(val address: ChallengeAddress) {
        data class PlaceAccessibility(
            val placeAccessibilityId: String,
            val placeAccessibilityAddress: ChallengeAddress
        ) : Contribution(placeAccessibilityAddress)

        data class PlaceAccessibilityComment(
            val placeAccessibilityCommentId: String,
            val placeAccessibilityAddress: ChallengeAddress
        ) : Contribution(placeAccessibilityAddress)

        data class BuildingAccessibility(
            val buildingAccessibilityId: String,
            val buildingAccessibilityAddress: ChallengeAddress
        ) : Contribution(buildingAccessibilityAddress)

        data class BuildingAccessibilityComment(
            val buildingAccessibilityCommentId: String,
            val buildingAccessibilityAddress: ChallengeAddress
        ) : Contribution(buildingAccessibilityAddress)
    }

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

    fun hasJoined(userId: String, challengeId: String): Boolean {
        return challengeParticipationRepository.findByChallengeIdAndUserId(
            userId = userId,
            challengeId = challengeId
        ) != null
    }

    fun contributeToSatisfiedChallenges(
        userId: String,
        contribution: Contribution
    ): List<ChallengeContribution> {
        val myInProgressChallenges = getMyInProgressChallenges(userId)
        val satisfiedChallenges = myInProgressChallenges
            .filter { ch ->
                ch.conditions.firstOrNull { cond ->
                    cond.isSatisfied(
                        address = contribution.address,
                        actionType = when (contribution) {
                            is Contribution.PlaceAccessibility -> ChallengeActionCondition.Type.PLACE_ACCESSIBILITY
                            is Contribution.PlaceAccessibilityComment -> ChallengeActionCondition.Type.PLACE_ACCESSIBILITY_COMMENT
                            is Contribution.BuildingAccessibility -> ChallengeActionCondition.Type.BUILDING_ACCESSIBILITY
                            is Contribution.BuildingAccessibilityComment -> ChallengeActionCondition.Type.BUILDING_ACCESSIBILITY_COMMENT
                        }
                    )
                } != null
            }
        return satisfiedChallenges
            .mapNotNull {
                try {
                    doContributeToChallenge(
                        userId,
                        challenge = it,
                        contribution
                    )
                } catch (t: SccDomainException) {
                    null
                }
            }
    }

    @Suppress("ThrowsCount")
    private fun doContributeToChallenge(
        userId: String,
        challenge: Challenge,
        contribution: Contribution
    ): ChallengeContribution {
        userRepository.findByIdOrNull(id = userId) ?: throw SccDomainException("해당 유저가 존재하지 않습니다.")
        challengeParticipationRepository.findByChallengeIdAndUserId(userId = userId, challengeId = challenge.id)
            ?: throw SccDomainException("챌린지에 참여 중이 아닙니다.")
        if (clock.instant() < challenge.startsAt) {
            throw SccDomainException(
                "아직 챌린지가 오픈되지 않았습니다",
                errorCode = SccDomainException.ErrorCode.CHALLENGE_NOT_OPENED
            )
        }
        if (challenge.endsAt?.let { it < clock.instant() } == true) {
            throw SccDomainException("해당 챌린지는 종료되었습니다.", errorCode = SccDomainException.ErrorCode.CHALLENGE_CLOSED)
        }
        val challengeContribution = challengeContributionRepository.save(
            ChallengeContribution(
                id = EntityIdGenerator.generateRandom(),
                userId = userId,
                challengeId = challenge.id,
                placeAccessibilityId = (contribution as? Contribution.PlaceAccessibility)?.placeAccessibilityId,
                placeAccessibilityCommentId = (contribution as? Contribution.PlaceAccessibilityComment)?.placeAccessibilityCommentId,
                buildingAccessibilityId = (contribution as? Contribution.BuildingAccessibility)?.buildingAccessibilityId,
                buildingAccessibilityCommentId = (contribution as? Contribution.BuildingAccessibilityComment)?.buildingAccessibilityCommentId,
                createdAt = clock.instant(),
                updatedAt = clock.instant()
            )
        )
        val contributionsCount = challengeContributionRepository.countByChallengeId(challengeId = challenge.id)
        challengeRepository.save(
            challenge.also {
                it.isComplete = challenge.goal <= contributionsCount
            }
        )
        return challengeContribution
    }
}
