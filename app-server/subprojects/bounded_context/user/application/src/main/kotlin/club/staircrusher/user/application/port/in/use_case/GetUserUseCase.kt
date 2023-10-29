package club.staircrusher.user.application.port.`in`.use_case

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.application.port.out.persistence.UserRepository
import club.staircrusher.user.domain.model.User

@Component
class GetUserUseCase(
    private val transactionManager: TransactionManager,
    private val userRepository: UserRepository,
) {
    fun handle(userId: String): User = transactionManager.doInTransaction {
        return@doInTransaction userRepository.findById(userId)
    }
}
