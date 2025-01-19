package club.staircrusher.user.application.port.`in`.use_case

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.application.port.out.persistence.IdentifiedUserRepository
import club.staircrusher.user.domain.model.IdentifiedUser

@Component
class GetUserUseCase(
    private val transactionManager: TransactionManager,
    private val identifiedUserRepository: IdentifiedUserRepository,
) {
    fun handle(userId: String): IdentifiedUser = transactionManager.doInTransaction {
        return@doInTransaction identifiedUserRepository.findById(userId).get()
    }
}
