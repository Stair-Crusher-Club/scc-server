package club.staircrusher.challenge.application.port.`in`.use_case

import club.staircrusher.challenge.application.port.`in`.ChallengeService
import club.staircrusher.challenge.application.port.out.persistence.ChallengeRankRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeRepository
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.application.port.`in`.UserApplicationService

/**
 * Get the number of contributions needed to reach the next rank.
 * If the user is already at the highest rank, return 0.
 */
@Component
class GetCountForNextChallengeRankUseCase(
    private val transactionManager: TransactionManager,
    private val userApplicationService: UserApplicationService,
    private val challengeRepository: ChallengeRepository,
    private val challengeRankRepository: ChallengeRankRepository,
    private val challengeService: ChallengeService,
) {
    fun handle(challengeId: String, userId: String): Int  = transactionManager.doInTransaction {
        userApplicationService.getUser(userId) ?: throw SccDomainException("잘못된 계정입니다.")
        val challenge = challengeRepository.findByIdOrNull(challengeId) ?: throw SccDomainException("잘못된 챌린지입니다.")
        if (!challengeService.hasJoined(userId, challengeId)) {
            throw SccDomainException("참여하지 않은 챌린지 입니다.")
        }

        val currentRank = challengeRankRepository.findByUserId(challenge.id, userId)
        val rank = currentRank?.rank ?: challengeRankRepository.findLastRank(challenge.id) ?: 1

        if (rank == 1L) {
            0
        } else {
            val nextRank = challengeRankRepository.findNextRank(challenge.id, rank)!!
            nextRank.contributionCount - (currentRank?.contributionCount ?: 0)
        }
    }
}
