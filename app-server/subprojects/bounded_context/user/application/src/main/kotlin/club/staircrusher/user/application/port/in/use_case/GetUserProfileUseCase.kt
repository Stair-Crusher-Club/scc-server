package club.staircrusher.user.application.port.`in`.use_case

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.application.port.out.persistence.UserProfileRepository
import club.staircrusher.user.domain.model.UserProfile

@Component
class GetUserProfileUseCase(
    private val transactionManager: TransactionManager,
    private val userProfileRepository: UserProfileRepository,
) {
    fun handle(userId: String): UserProfile? = transactionManager.doInTransaction {
        return@doInTransaction userProfileRepository.findFirstByUserAccountId(userId)
    }
}
