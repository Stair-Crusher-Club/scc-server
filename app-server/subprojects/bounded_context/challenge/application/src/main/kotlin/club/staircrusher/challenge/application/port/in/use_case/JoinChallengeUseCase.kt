package club.staircrusher.challenge.application.port.`in`.use_case

import club.staircrusher.challenge.application.port.`in`.ChallengeService
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
class JoinChallengeUseCase(
    private val transactionManager: TransactionManager,
    private val challengeRepository: ChallengeRepository,
    private val challengeContributionRepository: ChallengeContributionRepository,
    private val challengeParticipationRepository: ChallengeParticipationRepository,
    private val challengeService: ChallengeService,
    private val clock: Clock
) {

    data class JoinChallengeResult(
        val challenge: Challenge,
        val contributionsCount: Int,
        val participationsCount: Int,
    )

    fun handle(userId: String, challengeId: String, passcode: String?): JoinChallengeResult =
        transactionManager.doInTransaction(TransactionIsolationLevel.REPEATABLE_READ) {
            val challenge = challengeRepository.findById(challengeId)
            if (challengeService.hasJoined(userId = userId, challengeId = challengeId)) {
                return@doInTransaction JoinChallengeResult(
                    challenge = challenge,
                    contributionsCount = challengeContributionRepository.countByChallengeId(challengeId).toInt(),
                    participationsCount = challengeParticipationRepository.userCountByChallengeId(challengeId).toInt()
                )
            }
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
            return@doInTransaction JoinChallengeResult(
                challenge = challenge,
                contributionsCount = challengeContributionRepository.countByChallengeId(challengeId).toInt(),
                participationsCount = challengeParticipationRepository.userCountByChallengeId(challengeId).toInt()
            )
        }
}
