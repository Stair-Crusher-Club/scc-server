package club.staircrusher.challenge.application.port.`in`.use_case

import club.staircrusher.challenge.application.port.out.persistence.ChallengeRepository
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class DeleteChallengeUseCase(
    private val transactionManager: TransactionManager,
    private val challengeRepository: ChallengeRepository,
) {
    fun handle(challengeId: String) = transactionManager.doInTransaction {
        // TODO: 삭제 불가능 여부 체크하기; e.g. 이미 join한 유저가 있는 등.
        // TODO: 필요한 경우 다른 리소스도 삭제하기; e.g. challenge condition?
        challengeRepository.remove(challengeId)
    }
}
