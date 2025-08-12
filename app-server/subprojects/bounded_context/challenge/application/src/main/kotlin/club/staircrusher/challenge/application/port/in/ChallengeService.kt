package club.staircrusher.challenge.application.port.`in`

import club.staircrusher.challenge.application.port.out.persistence.ChallengeContributionRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeParticipationRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeRepository
import club.staircrusher.challenge.domain.model.Challenge
import club.staircrusher.challenge.domain.model.ChallengeActionCondition
import club.staircrusher.challenge.domain.model.ChallengeAddress
import club.staircrusher.challenge.domain.model.ChallengeContribution
import club.staircrusher.challenge.domain.model.ChallengeCrusherGroup
import club.staircrusher.challenge.domain.model.ChallengeStatus
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.user.application.port.out.persistence.UserAccountRepository
import org.springframework.data.repository.findByIdOrNull
import java.time.Clock
import java.time.Instant

@Component
class ChallengeService(
    private val userAccountRepository: UserAccountRepository,
    private val challengeRepository: ChallengeRepository,
    private val challengeContributionRepository: ChallengeContributionRepository,
    private val challengeParticipationRepository: ChallengeParticipationRepository,
    private val clock: Clock,
) {
    fun getMyInProgressChallenges(userId: String, criteriaTime: Instant = clock.instant()): List<Challenge> {
        return challengeRepository.findByUidAndTime(
            userId = userId,
            startsAtFrom = Challenge.MIN_TIME,
            startsAtTo = criteriaTime,
            endsAtFrom = criteriaTime,
            endsAtTo = Challenge.MAX_TIME,
        )
            .filter { it.getStatus(criteriaTime) == ChallengeStatus.IN_PROGRESS }
    }

    fun getInProgressChallenges(criteriaTime: Instant = clock.instant()): List<Challenge> {
        return challengeRepository.findByTime(
            startsAtFrom = Challenge.MIN_TIME,
            startsAtTo = criteriaTime,
            endsAtFrom = criteriaTime,
            endsAtTo = Challenge.MAX_TIME,
        )
            .filter { it.getStatus(criteriaTime) == ChallengeStatus.IN_PROGRESS }
    }

    fun getUpcomingChallenges(criteriaTime: Instant = clock.instant()): List<Challenge> {
        return challengeRepository.findByTime(
            startsAtFrom = criteriaTime,
            startsAtTo = Challenge.MAX_TIME,
            endsAtFrom = criteriaTime,
            endsAtTo = Challenge.MAX_TIME,
        )
            .filter { it.getStatus(criteriaTime) == ChallengeStatus.UPCOMING }
    }

    fun getClosedChallenges(criteriaTime: Instant = clock.instant()): List<Challenge> {
        return challengeRepository.findByTime(
            startsAtFrom = Challenge.MIN_TIME,
            startsAtTo = criteriaTime,
            endsAtFrom = Challenge.MIN_TIME,
            endsAtTo = criteriaTime,
        )
            .filter { it.getStatus(criteriaTime) == ChallengeStatus.CLOSED }
    }

    fun hasJoined(userId: String, challengeId: String): Boolean {
        return challengeParticipationRepository.findByChallengeIdAndUserId(
            userId = userId,
            challengeId = challengeId
        ).isNotEmpty()
    }

    fun getPlaceAccessibilityCrusherGroup(placeAccessibilityId: String): ChallengeCrusherGroup? {
        val contributions = challengeContributionRepository.findByPlaceAccessibilityId(placeAccessibilityId)
        if (contributions.isEmpty()) return null
        val challenges = challengeRepository.findAllById(contributions.map { it.challengeId })
        return challenges.firstNotNullOfOrNull { it.crusherGroup }
    }

    fun getBuildingAccessibilityCrusherGroup(buildingAccessibilityId: String): ChallengeCrusherGroup? {
        val contributions = challengeContributionRepository.findByBuildingAccessibilityId(buildingAccessibilityId)
        if (contributions.isEmpty()) return null
        val challenges = challengeRepository.findAllById(contributions.map { it.challengeId })
        return challenges.firstNotNullOfOrNull { it.crusherGroup }
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
                        actionType = contribution.actionType,
                    )
                } != null
            }
        return satisfiedChallenges
            .mapNotNull { challenge ->
                try {
                    doContributeToChallenge(userId, challenge = challenge, contribution)
                } catch (@Suppress("SwallowedException") t: SccDomainException) {
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
        userAccountRepository.findByIdOrNull(userId) ?: throw SccDomainException("해당 유저가 존재하지 않습니다.")
        if (challengeParticipationRepository.findByChallengeIdAndUserId(userId = userId, challengeId = challenge.id)
                .isEmpty()
        ) {
            throw SccDomainException("챌린지에 참여 중이 아닙니다.")
        }
        if (clock.instant() < challenge.startsAt) {
            throw SccDomainException(
                "아직 챌린지가 오픈되지 않았습니다",
                errorCode = SccDomainException.ErrorCode.CHALLENGE_NOT_OPENED
            )
        }
        if (challenge.endsAt?.let { it < clock.instant() } == true) {
            throw SccDomainException("해당 챌린지는 종료되었습니다.", errorCode = SccDomainException.ErrorCode.CHALLENGE_CLOSED)
        }

        val alreadyContributed = getExistingContribution(challengeId = challenge.id, contribution = contribution)
        if (alreadyContributed != null) return alreadyContributed

        val challengeContribution = challengeContributionRepository.save(
            ChallengeContribution(
                id = EntityIdGenerator.generateRandom(),
                userId = userId,
                challengeId = challenge.id,
                placeAccessibilityId = (contribution as? Contribution.PlaceAccessibility)?.placeAccessibilityId,
                placeAccessibilityCommentId = (contribution as? Contribution.PlaceAccessibilityComment)?.placeAccessibilityCommentId,
                buildingAccessibilityId = (contribution as? Contribution.BuildingAccessibility)?.buildingAccessibilityId,
                buildingAccessibilityCommentId = (contribution as? Contribution.BuildingAccessibilityComment)?.buildingAccessibilityCommentId,
                placeReviewId = (contribution as? Contribution.PlaceReview)?.placeReviewId,
                createdAt = clock.instant(),
                updatedAt = clock.instant()
            )
        )

        // 퀘스트 진행도 업데이트
        updateQuestProgress(
            userId = userId,
            challengeId = challenge.id,
            contributionId = challengeContribution.id,
            actionType = contribution.actionType,
            placeCategory = contribution.placeCategory,
            challenge = challenge,
            contributionCreatedAt = challengeContribution.createdAt
        )

        val contributionsCount = challengeContributionRepository.countByChallengeId(challengeId = challenge.id)
        challengeRepository.save(
            challenge.also {
                it.isComplete = challenge.goal <= contributionsCount
            }
        )
        return challengeContribution
    }

    private fun getExistingContribution(challengeId: String, contribution: Contribution): ChallengeContribution? {
        return when (contribution) {
            is Contribution.PlaceAccessibility -> {
                challengeContributionRepository.findFirstByChallengeIdAndPlaceAccessibilityId(
                    challengeId = challengeId,
                    placeAccessibilityId = contribution.placeAccessibilityId
                )
            }

            is Contribution.PlaceAccessibilityComment -> {
                challengeContributionRepository.findFirstByChallengeIdAndPlaceAccessibilityCommentId(
                    challengeId = challengeId,
                    placeAccessibilityCommentId = contribution.placeAccessibilityCommentId
                )
            }

            is Contribution.BuildingAccessibility -> {
                challengeContributionRepository.findFirstByChallengeIdAndBuildingAccessibilityId(
                    challengeId = challengeId,
                    buildingAccessibilityId = contribution.buildingAccessibilityId
                )
            }

            is Contribution.BuildingAccessibilityComment -> {
                challengeContributionRepository.findFirstByChallengeIdAndBuildingAccessibilityCommentId(
                    challengeId = challengeId,
                    buildingAccessibilityCommentId = contribution.buildingAccessibilityCommentId
                )
            }

            is Contribution.PlaceReview -> {
                challengeContributionRepository.findFirstByChallengeIdAndPlaceReviewId(
                    challengeId = challengeId,
                    placeReviewId = contribution.placeReviewId
                )
            }
        }
    }

    fun deleteContributions(
        userId: String,
        contribution: Contribution,
    ) {
        val myInProgressChallenges = getMyInProgressChallenges(userId = userId)
        myInProgressChallenges
            .mapNotNull { myInProgressChallenge ->
                getExistingContribution(myInProgressChallenge.id, contribution)?.let { contrib ->
                    myInProgressChallenge to contrib
                }
            }
            .forEach { (challenge, challengeContribution) ->
                // 퀘스트 진행도에서 제거
                removeQuestProgress(
                    userId = userId,
                    challengeId = challenge.id,
                    contributionId = challengeContribution.id,
                    challenge = challenge
                )

                challengeContributionRepository.deleteById(challengeContribution.id)
            }
    }

    private fun updateQuestProgress(
        userId: String,
        challengeId: String,
        contributionId: String,
        actionType: ChallengeActionCondition.Type,
        placeCategory: String?,
        challenge: Challenge,
        contributionCreatedAt: Instant
    ) {
        val challengeQuests = challenge.quests
        if (challengeQuests.isNullOrEmpty()) return

        val participation = challengeParticipationRepository.findByChallengeIdAndUserId(
            userId = userId,
            challengeId = challengeId
        ).firstOrNull() ?: return

        participation.updateQuestProgress(
            challengeQuests = challengeQuests,
            contributionId = contributionId,
            actionType = actionType,
            placeCategory = placeCategory,
            challengeStartsAt = challenge.startsAt,
            challengeEndsAt = challenge.endsAt,
            contributionCreatedAt = contributionCreatedAt
        )
        challengeParticipationRepository.save(participation)
    }

    private fun removeQuestProgress(
        userId: String,
        challengeId: String,
        contributionId: String,
        challenge: Challenge
    ) {
        val challengeQuests = challenge.quests
        if (challengeQuests.isNullOrEmpty()) return

        val participation = challengeParticipationRepository.findByChallengeIdAndUserId(
            userId = userId,
            challengeId = challengeId
        ).firstOrNull() ?: return

        participation.removeQuestProgress(contributionId, challengeQuests)
        challengeParticipationRepository.save(participation)
    }

    sealed class Contribution(val address: ChallengeAddress, val placeCategory: String?) {
        abstract val actionType: ChallengeActionCondition.Type

        data class PlaceAccessibility(
            val placeAccessibilityId: String,
            val placeAccessibilityAddress: ChallengeAddress,
            val placeCategoryValue: String?,
        ) : Contribution(placeAccessibilityAddress, placeCategoryValue) {
            override val actionType = ChallengeActionCondition.Type.PLACE_ACCESSIBILITY
        }

        data class PlaceAccessibilityComment(
            val placeAccessibilityCommentId: String,
            val placeAccessibilityAddress: ChallengeAddress,
            val placeCategoryValue: String?,
        ) : Contribution(placeAccessibilityAddress, placeCategoryValue) {
            override val actionType = ChallengeActionCondition.Type.PLACE_ACCESSIBILITY_COMMENT
        }

        data class BuildingAccessibility(
            val buildingAccessibilityId: String,
            val buildingAccessibilityAddress: ChallengeAddress,
        ) : Contribution(buildingAccessibilityAddress, placeCategory = null) {
            override val actionType = ChallengeActionCondition.Type.BUILDING_ACCESSIBILITY
        }

        data class BuildingAccessibilityComment(
            val buildingAccessibilityCommentId: String,
            val buildingAccessibilityAddress: ChallengeAddress,
        ) : Contribution(buildingAccessibilityAddress, placeCategory = null) {
            override val actionType = ChallengeActionCondition.Type.BUILDING_ACCESSIBILITY_COMMENT
        }

        data class PlaceReview(
            val placeReviewId: String,
            val placeReviewAddress: ChallengeAddress,
            val placeCategoryValue: String?,
        ) : Contribution(placeReviewAddress, placeCategoryValue) {
            override val actionType = ChallengeActionCondition.Type.PLACE_REVIEW
        }
    }
}
