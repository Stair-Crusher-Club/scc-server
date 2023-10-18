package club.staircrusher.challenge.application.port.`in`.use_case

import club.staircrusher.challenge.application.port.`in`.ChallengeService
import club.staircrusher.challenge.application.port.out.persistence.ChallengeRankRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeRepository
import club.staircrusher.challenge.domain.model.ChallengeRank
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.application.port.`in`.UserApplicationService
import java.util.UUID

@Component
class GetChallengeRankUseCase(
    private val transactionManager: TransactionManager,
    private val userApplicationService: UserApplicationService,
    private val challengeRepository: ChallengeRepository,
    private val challengeRankRepository: ChallengeRankRepository,
    private val challengeService: ChallengeService,
) {
    fun handle(challengeId: String, userId: String): ChallengeRank = transactionManager.doInTransaction {
        val now = SccClock.instant()
        val user = userApplicationService.getUser(userId) ?: throw SccDomainException("잘못된 계정입니다.")
        val challenge = challengeRepository.findByIdOrNull(challengeId) ?: throw SccDomainException("잘못된 챌린지입니다.")
        if (!challengeService.hasJoined(userId, challengeId)) {
            throw SccDomainException("참여하지 않은 챌린지 입니다.")
        }

        val challengeRank = challengeRankRepository.findByUserId(challenge.id, userId) ?: run {
            // if lastRank can not be found, then the user is the first rank
            val lastRank = challengeRankRepository.findLastRank(challengeId) ?: 1
            ChallengeRank(
                id = UUID.randomUUID().toString(),
                challengeId = challenge.id,
                userId = user.id,
                contributionCount = 0,
                rank = lastRank,
                createdAt = now,
                updatedAt = now,
            )
        }

        challengeRank
    }
}
