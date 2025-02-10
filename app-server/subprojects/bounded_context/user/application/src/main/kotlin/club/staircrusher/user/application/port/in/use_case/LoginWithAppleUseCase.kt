package club.staircrusher.user.application.port.`in`.use_case

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.application.port.`in`.InitialNicknameGenerator
import club.staircrusher.user.application.port.`in`.UserApplicationService
import club.staircrusher.user.application.port.out.persistence.UserAuthInfoRepository
import club.staircrusher.user.application.port.out.persistence.UserProfileRepository
import club.staircrusher.user.application.port.out.web.login.apple.AppleLoginService
import club.staircrusher.user.domain.model.UserAuthInfo
import club.staircrusher.user.domain.model.UserAuthProviderType
import club.staircrusher.user.domain.service.UserAuthService
import kotlinx.coroutines.runBlocking
import java.time.Duration

@Component
class LoginWithAppleUseCase(
    private val transactionManager: TransactionManager,
    private val appleLoginService: AppleLoginService,
    private val userProfileRepository: UserProfileRepository,
    private val userAuthInfoRepository: UserAuthInfoRepository,
    private val userAuthService: UserAuthService,
    private val userApplicationService: UserApplicationService,
) {
    fun handle(authorizationCode: String): LoginResult = transactionManager.doInTransaction {
        val appleLoginTokens = runBlocking {
            appleLoginService.getAppleLoginTokens(authorizationCode)
        }

        val userAuthInfo = userAuthInfoRepository.findFirstByAuthProviderTypeAndExternalId(UserAuthProviderType.APPLE, appleLoginTokens.idToken.appleLoginUserId)
        if (userAuthInfo != null) {
            doLoginForExistingUser(userAuthInfo)
        } else {
            doLoginWithSignUp(appleLoginTokens.refreshToken, appleLoginTokens.idToken.appleLoginUserId)
        }
    }

    private fun doLoginForExistingUser(userAuthInfo: UserAuthInfo): LoginResult {
        val authTokens = userAuthService.issueTokens(userAuthInfo)
        val user = userProfileRepository.findById(userAuthInfo.userId).get()
        return LoginResult(
            authTokens = authTokens,
            user = user,
        )
    }

    private fun doLoginWithSignUp(appleRefreshToken: String, appleLoginUserId: String): LoginResult {
        val user = userApplicationService.signUp(
            params = UserProfileRepository.CreateUserParams(
                nickname = InitialNicknameGenerator.generate(),
                password = null,
                instagramId = null,
                email = null,
            )
        )

        val newUserAuthInfo = userAuthInfoRepository.save(
            UserAuthInfo(
                id = EntityIdGenerator.generateRandom(),
                userId = user.id,
                authProviderType = UserAuthProviderType.APPLE,
                externalId = appleLoginUserId,
                externalRefreshToken = appleRefreshToken,
                externalRefreshTokenExpiresAt = SccClock.instant() + appleRefreshTokenExpirationDuration,
            )
        )

        val authTokens = userAuthService.issueTokens(newUserAuthInfo)
        return LoginResult(
            authTokens = authTokens,
            user = user,
        )
    }

    companion object {
        private val appleRefreshTokenExpirationDuration = Duration.ofDays(30)
    }
}
