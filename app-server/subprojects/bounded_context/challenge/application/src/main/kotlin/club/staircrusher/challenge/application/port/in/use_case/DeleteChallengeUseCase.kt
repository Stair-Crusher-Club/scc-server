package club.staircrusher.challenge.application.port.`in`.use_case

import club.staircrusher.challenge.application.port.out.persistence.ChallengeContributionRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeParticipationRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeRepository
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class DeleteChallengeUseCase(
    private val transactionManager: TransactionManager,
    private val challengeRepository: ChallengeRepository,
    private val challengeParticipationRepository: ChallengeParticipationRepository,
    private val challengeContributionRepository: ChallengeContributionRepository,
) {
    fun handle(challengeId: String) = transactionManager.doInTransaction {
        if (challengeParticipationRepository.findByChallengeId(challengeId).isNotEmpty()) {
            throw SccDomainException("이미 참여자가 존재하는 챌린지는 삭제할 수 없습니다.")
        }
        if (challengeContributionRepository.findByChallengeId(challengeId).isNotEmpty()) {
            throw SccDomainException("이미 기여가 있는 챌린지는 삭제할 수 없습니다.")
        }
        challengeRepository.deleteById(challengeId)
    }
}
