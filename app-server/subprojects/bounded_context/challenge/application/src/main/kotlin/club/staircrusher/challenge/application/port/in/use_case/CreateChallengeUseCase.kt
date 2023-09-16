package club.staircrusher.challenge.application.port.`in`.use_case

import club.staircrusher.challenge.application.port.out.persistence.ChallengeRepository
import club.staircrusher.challenge.domain.model.Challenge
import club.staircrusher.challenge.domain.model.CreateChallengeRequest
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class CreateChallengeUseCase(
    private val transactionManager: TransactionManager,
    private val challengeRepository: ChallengeRepository,
) {
    fun handle(createRequest: CreateChallengeRequest): Challenge = transactionManager.doInTransaction {
        challengeRepository.save(Challenge.of(createRequest))
    }
}
