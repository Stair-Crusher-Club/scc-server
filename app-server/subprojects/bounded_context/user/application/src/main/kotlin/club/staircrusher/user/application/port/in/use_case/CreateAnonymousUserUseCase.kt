package club.staircrusher.user.application.port.`in`.use_case

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.application.port.`in`.UserApplicationService
import club.staircrusher.user.domain.model.AuthTokens
import club.staircrusher.user.domain.service.UserAuthService

@Component
class CreateAnonymousUserUseCase(
    private val transactionManager: TransactionManager,
    private val userApplicationService: UserApplicationService,
    private val userAuthService: UserAuthService,
) {
    fun handle(): AuthTokens = transactionManager.doInTransaction {
        val anonymousUser = userApplicationService.createAnonymousUser()
        val accessToken = userAuthService.issueAnonymousAccessToken(anonymousUser.id)

        AuthTokens(accessToken, userId = anonymousUser.id)
    }
}
