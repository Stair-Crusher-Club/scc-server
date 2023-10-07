package club.staircrusher.challenge.application.port.`in`

import club.staircrusher.challenge.application.port.out.persistence.ChallengeContributionRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeParticipationRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeRepository
import club.staircrusher.challenge.domain.model.Challenge
import club.staircrusher.challenge.domain.model.ChallengeParticipation
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager
import java.time.Clock

@Component
class ChallengeService(
    private val transactionManager: TransactionManager,
    private val challengeRepository: ChallengeRepository,
    private val challengeContributionRepository: ChallengeContributionRepository,
    private val challengeParticipationRepository: ChallengeParticipationRepository,
    private val clock: Clock,
) {
    sealed class MyChallengeOption {
        data class Only(val userId: String) : MyChallengeOption()
        data class Without(val userId: String) : MyChallengeOption()
    }

    data class GetChallengeResult(
        val challenge: Challenge,
        val contributionsCount: Int,
        val participationsCount: Int,
        val hasJoined: Boolean
    )

    fun getInProgressChallenges(option: MyChallengeOption? = null): List<Challenge> {
        return transactionManager.doInTransaction {
            return@doInTransaction when (option) {
                is MyChallengeOption.Only ->
                    challengeRepository.joinedChallenges(
                        userId = option.userId,
                        startsAtRange = Challenge.MIN_TIME.rangeTo(clock.instant()),
                        endsAtRange = clock.instant().rangeTo(Challenge.MAX_TIME),
                    )

                is MyChallengeOption.Without ->
                    challengeRepository.notJoinedChallenges(
                        userId = option.userId,
                        startsAtRange = Challenge.MIN_TIME.rangeTo(clock.instant()),
                        endsAtRange = clock.instant().rangeTo(Challenge.MAX_TIME),
                    )

                null -> challengeRepository.findByTime(
                    startsAtRange = Challenge.MIN_TIME.rangeTo(clock.instant()),
                    endsAtRange = clock.instant().rangeTo(Challenge.MAX_TIME),
                )
            }
        }
    }

    fun getUpcomingChallenges(): List<Challenge> {
        return transactionManager.doInTransaction {
            return@doInTransaction challengeRepository.findByTime(
                startsAtRange = clock.instant().rangeTo(Challenge.MAX_TIME),
                endsAtRange = clock.instant().rangeTo(Challenge.MAX_TIME),
            )
        }
    }

    fun getClosedChallenges(): List<Challenge> {
        return transactionManager.doInTransaction {
            return@doInTransaction challengeRepository.findByTime(
                startsAtRange = Challenge.MIN_TIME.rangeTo(clock.instant()),
                endsAtRange = Challenge.MIN_TIME.rangeTo(clock.instant()),
            )
        }
    }

    fun getChallenge(
        userId: String?,
        challengeId: String?,
        invitationCode: String?
    ): GetChallengeResult {
        if (challengeId == null && invitationCode == null)
            throw SccDomainException(
                msg = "챌린지 초대코드나 ID 가 필요합니다.",
                errorCode = SccDomainException.ErrorCode.INVALID_ARGUMENTS
            )

        return transactionManager.doInTransaction {
            val challenge = challengeId?.let { challengeRepository.findById(id = it) }
                ?: invitationCode?.let { challengeRepository.findByInvitationCode(it) }
                ?: throw SccDomainException(
                    msg = "해당 챌린지가 없습니다.",
                    errorCode = SccDomainException.ErrorCode.INVALID_ARGUMENTS
                )
            val participationsCount =
                challengeParticipationRepository.userCountByChallengeId(challengeId = challenge.id)
            val contributionsCount = challengeContributionRepository.countByChallengeId(challengeId = challenge.id)
            return@doInTransaction GetChallengeResult(
                challenge = challenge,
                contributionsCount = contributionsCount.toInt(),
                participationsCount = participationsCount.toInt(),
                hasJoined = userId?.let {
                    challengeParticipationRepository
                        .findByChallengeIdAndUserId(userId = it, challengeId = challenge.id)
                } != null
            )
        }
    }

    fun getChallenge(
        userId: String?,
        challengeId: String?,
        invitationCode: String?
    ): GetChallengeResult {
        if (challengeId == null && invitationCode == null)
            throw SccDomainException(
                msg = "챌린지 초대코드나 ID 가 필요합니다.",
                errorCode = SccDomainException.ErrorCode.INVALID_ARGUMENTS
            )

        return transactionManager.doInTransaction {
            val challenge = challengeId?.let { challengeRepository.findById(id = it) }
                ?: invitationCode?.let { challengeRepository.findByInvitationCode(it) }
                ?: throw SccDomainException(
                    msg = "해당 챌린지가 없습니다.",
                    errorCode = SccDomainException.ErrorCode.INVALID_ARGUMENTS
                )
            val participationsCount =
                challengeParticipationRepository.userCountByChallengeId(challengeId = challenge.id)
            val contributionsCount = challengeContributionRepository.countByChallengeId(challengeId = challenge.id)
            return@doInTransaction GetChallengeResult(
                challenge = challenge,
                contributionsCount = contributionsCount.toInt(),
                participationsCount = participationsCount.toInt(),
                hasJoined = userId?.let {
                    challengeParticipationRepository
                        .findByChallengeIdAndUserId(userId = it, challengeId = challenge.id)
                } != null
            )
        }
    }

    @Suppress("ThrowsCount")
    fun joinChallenge(userId: String, challengeId: String, passcode: String?): Challenge {
        return transactionManager.doInTransaction(TransactionIsolationLevel.REPEATABLE_READ) {
            val alreadyJoined = challengeParticipationRepository.findByChallengeIdAndUserId(challengeId, userId) != null
            if (alreadyJoined) {
                throw SccDomainException(
                    msg = "이미 참여한 챌린지입니다.",
                    errorCode = SccDomainException.ErrorCode.ALREADY_JOINED
                )
            }
            val challenge = challengeRepository.findById(challengeId)
            if (challenge.passcode != null && challenge.passcode != passcode) {
                throw SccDomainException(
                    msg = "잘못된 참여코드 입니다.",
                    errorCode = SccDomainException.ErrorCode.INVALID_PASSCODE
                )
            }
            val now = clock.instant()
            if (now < challenge.startsAt) {
                throw SccDomainException(
                    msg = "아직 오픈 전 입니다.",
                    errorCode = SccDomainException.ErrorCode.CHALLENGE_NOT_OPENED
                )
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
