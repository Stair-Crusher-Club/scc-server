package club.staircrusher.challenge.application.port.`in`.use_case

import club.staircrusher.challenge.application.port.`in`.ChallengeService
import club.staircrusher.challenge.application.port.out.persistence.ChallengeContributionRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeParticipationRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeRepository
import club.staircrusher.challenge.domain.model.Challenge
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class GetChallengeUseCase(
    private val transactionManager: TransactionManager,
    private val challengeService: ChallengeService,
    private val challengeRepository: ChallengeRepository,
    private val challengeContributionRepository: ChallengeContributionRepository,
    private val challengeParticipationRepository: ChallengeParticipationRepository
) {
    data class GetChallengeResult(
        val challenge: Challenge,
        val contributionsCount: Int,
        val participationsCount: Int,
        val hasJoined: Boolean
    )

    fun handle(userId: String? = null, challengeId: String): GetChallengeResult = transactionManager.doInTransaction {
        val challenge = challengeRepository.findById(challengeId)
        val participationsCount = challengeParticipationRepository.userCountByChallengeId(challengeId = challenge.id)
        val contributionsCount = challengeContributionRepository.countByChallengeId(challengeId = challenge.id)
        return@doInTransaction GetChallengeResult(
            challenge = challenge,
            contributionsCount = contributionsCount.toInt(),
            participationsCount = participationsCount.toInt(),
            hasJoined = userId?.let { challengeService.hasJoined(userId, challenge.id) } ?: false
        )
    }
}
