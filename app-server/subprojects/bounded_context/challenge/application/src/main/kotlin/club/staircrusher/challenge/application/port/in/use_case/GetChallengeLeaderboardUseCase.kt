package club.staircrusher.challenge.application.port.`in`.use_case

import club.staircrusher.challenge.application.port.`in`.result.WithUserInfo
import club.staircrusher.challenge.application.port.`in`.toDomainModel
import club.staircrusher.challenge.application.port.out.persistence.ChallengeRankRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeRepository
import club.staircrusher.challenge.domain.model.ChallengeRank
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.application.port.`in`.UserApplicationService
import org.springframework.data.repository.findByIdOrNull

/**
 * Get the leaderboard of a challenge which shows only the top 10 users.
 * If there are more than 2 users with the same score, those users' ranks
 * are the same and the next rank is skipped. For example, if there are
 * 3 users with the same score, the ranks are 1, 1, 1, 4, 5, 6, 7, 8, 9, 10.
 */
@Component
class GetChallengeLeaderboardUseCase(
    private val transactionManager: TransactionManager,
    private val challengeRepository: ChallengeRepository,
    private val challengeRankRepository: ChallengeRankRepository,
    private val userApplicationService: UserApplicationService,
) {
    companion object {
        const val NUMBER_OF_TOP_RANKER = 10
    }

    fun handle(challengeId: String): List<WithUserInfo<ChallengeRank>> = transactionManager.doInTransaction {
        val challenge = challengeRepository.findByIdOrNull(challengeId) ?: throw SccDomainException("잘못된 챌린지입니다.")
        val leaderboards = challengeRankRepository.findTopNUsers(challenge.id, NUMBER_OF_TOP_RANKER)
        val userProfilesByUserId = userApplicationService.getProfilesByUserIds(leaderboards.map { it.userId }).associateBy { it.userId }

        leaderboards.map { challengeRank ->
            WithUserInfo(challengeRank, userProfilesByUserId[challengeRank.userId]!!.toDomainModel())
        }
    }
}
