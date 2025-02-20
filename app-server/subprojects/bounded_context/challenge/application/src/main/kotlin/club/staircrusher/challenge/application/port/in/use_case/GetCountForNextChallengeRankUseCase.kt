package club.staircrusher.challenge.application.port.`in`.use_case

import club.staircrusher.challenge.application.port.`in`.ChallengeService
import club.staircrusher.challenge.application.port.out.persistence.ChallengeRankRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeRepository
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.application.port.`in`.UserApplicationService
import org.springframework.data.repository.findByIdOrNull

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
    fun handle(challengeId: String, userId: String): Long?  = transactionManager.doInTransaction {
        userApplicationService.getUserProfileOrNull(userId) ?: throw SccDomainException("잘못된 계정입니다.")
        val challenge = challengeRepository.findByIdOrNull(challengeId) ?: throw SccDomainException("잘못된 챌린지입니다.")
        if (!challengeService.hasJoined(userId, challengeId)) {
            throw SccDomainException("참여하지 않은 챌린지 입니다.")
        }

        // if the user does not have rank yet, return null and let the user know that the rank will be updated soon
        val challengeRank = challengeRankRepository.findFirstByChallengeIdAndUserId(challenge.id, userId)
        val rank =  challengeRank?.rank ?: return@doInTransaction null

        if (rank == 1L) {
            0
        } else {
            val nextRank = challengeRankRepository.findNextRank(challenge.id, rank)!!
            nextRank.contributionCount - challengeRank.contributionCount
        }
    }
}
