package club.staircrusher.challenge.application.port.`in`.use_case

import club.staircrusher.challenge.application.port.out.persistence.ChallengeRepository
import club.staircrusher.challenge.domain.model.Challenge
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class ListAllChallengesUseCase(
    private val transactionManager: TransactionManager,
    private val challengeRepository: ChallengeRepository,
) {
    fun handle(): List<Challenge> = transactionManager.doInTransaction {
        challengeRepository.findAllByOrderByCreatedAtDesc()
    }
}
