package club.staircrusher.user.application.port.`in`

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.application.port.out.persistence.UserAccountRepository
import club.staircrusher.user.domain.exception.UserAuthenticationException
import club.staircrusher.user.domain.service.UserAuthService
import org.springframework.data.repository.findByIdOrNull

@Component
class UserAuthApplicationService(
    private val transactionManager: TransactionManager,
    private val userAccountRepository: UserAccountRepository,
    private val userAuthService: UserAuthService,
) {
    // User ID를 반환한다.
    fun verify(accessToken: String?): String = transactionManager.doInTransaction {
        accessToken ?: throw UserAuthenticationException()
        val userId = userAuthService.verifyAccessToken(accessToken).userId
        userAccountRepository.findByIdOrNull(userId) ?: throw UserAuthenticationException()
        userId
    }
}
