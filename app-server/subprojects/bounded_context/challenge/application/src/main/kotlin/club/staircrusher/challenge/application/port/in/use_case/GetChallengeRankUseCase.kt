package club.staircrusher.challenge.application.port.`in`.use_case

import club.staircrusher.challenge.application.port.`in`.ChallengeService
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

@Component
class GetChallengeRankUseCase(
    private val transactionManager: TransactionManager,
    private val userApplicationService: UserApplicationService,
    private val challengeRepository: ChallengeRepository,
    private val challengeRankRepository: ChallengeRankRepository,
    private val challengeService: ChallengeService,
) {
    fun handle(challengeId: String, userId: String): WithUserInfo<ChallengeRank>? = transactionManager.doInTransaction {
        val user = userApplicationService.getUserProfileOrNull(userId) ?: throw SccDomainException("잘못된 계정입니다.")
        val challenge = challengeRepository.findByIdOrNull(challengeId) ?: throw SccDomainException("잘못된 챌린지입니다.")
        if (!challengeService.hasJoined(userId, challengeId)) {
            throw SccDomainException("참여하지 않은 챌린지 입니다.")
        }

        // if the user does not have rank yet, return null and let the user know that the rank will be updated soon
        challengeRankRepository.findFirstByChallengeIdAndUserId(challenge.id, userId)?.let { WithUserInfo(it, user.toDomainModel()) }
    }
}
