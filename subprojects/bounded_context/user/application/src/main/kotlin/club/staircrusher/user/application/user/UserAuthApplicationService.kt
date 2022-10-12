package club.staircrusher.user.application.user

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.domain.exception.UserAuthenticationException
import club.staircrusher.user.domain.repository.UserRepository
import club.staircrusher.user.domain.service.UserAuthService

@Component
class UserAuthApplicationService(
    private val transactionManager: TransactionManager,
    private val userRepository: UserRepository,
    private val userAuthService: UserAuthService,
) {
    // User ID를 반환한다.
    fun verify(accessToken: String?): String = transactionManager.doInTransaction {
        accessToken ?: throw UserAuthenticationException()
        val userId = userAuthService.verifyAccessToken(accessToken).userId
        userRepository.findByIdOrNull(userId) ?: throw UserAuthenticationException()
        userId
    }
}
