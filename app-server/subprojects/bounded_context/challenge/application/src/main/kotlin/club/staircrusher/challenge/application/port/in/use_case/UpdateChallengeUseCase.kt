package club.staircrusher.challenge.application.port.`in`.use_case

import club.staircrusher.challenge.application.port.out.persistence.ChallengeRepository
import club.staircrusher.challenge.domain.model.Challenge
import club.staircrusher.challenge.domain.model.UpdateChallengeRequest
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.stdlib.persistence.TransactionManager
import org.springframework.data.repository.findByIdOrNull

@Component
class UpdateChallengeUseCase(
    private val transactionManager: TransactionManager,
    private val challengeRepository: ChallengeRepository,
) {
    fun handle(updateRequest: UpdateChallengeRequest): Challenge = transactionManager.doInTransaction {
        val updatingChallenge = challengeRepository.findByIdOrNull(updateRequest.id) ?: throw SccDomainException("챌린지를 찾을 수 없습니다.")

        updatingChallenge.update(updateRequest)
        challengeRepository.save(updatingChallenge)
    }
}
