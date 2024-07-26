package club.staircrusher.user.application.port.`in`.use_case

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.application.port.`in`.InitialNicknameGenerator
import club.staircrusher.user.application.port.`in`.UserApplicationService
import club.staircrusher.user.application.port.out.persistence.UserAuthInfoRepository
import club.staircrusher.user.application.port.out.persistence.UserRepository
import club.staircrusher.user.application.port.out.web.login.kakao.KakaoLoginService
import club.staircrusher.user.domain.model.UserAuthInfo
import club.staircrusher.user.domain.model.UserAuthProviderType
import club.staircrusher.user.domain.service.UserAuthService
import java.time.Duration

@Component
class LoginWithKakaoUseCase(
    private val transactionManager: TransactionManager,
    private val kakaoLoginService: KakaoLoginService,
    private val userRepository: UserRepository,
    private val userAuthInfoRepository: UserAuthInfoRepository,
    private val userAuthService: UserAuthService,
    private val userApplicationService: UserApplicationService,
) {
    fun handle(kakaoRefreshToken: String, rawKakaoIdToken: String): LoginResult = transactionManager.doInTransaction {
        val idToken = kakaoLoginService.parseIdToken(rawKakaoIdToken)

        val userAuthInfo = userAuthInfoRepository.findFirstByAuthProviderTypeAndExternalId(UserAuthProviderType.KAKAO, idToken.kakaoSyncUserId)
        if (userAuthInfo != null) {
            doLoginForExistingUser(userAuthInfo)
        } else {
            doLoginWithSignUp(kakaoRefreshToken, idToken.kakaoSyncUserId)
        }
    }

    private fun doLoginForExistingUser(userAuthInfo: UserAuthInfo): LoginResult {
        val authTokens = userAuthService.issueTokens(userAuthInfo)
        val user = userRepository.findById(userAuthInfo.userId).get()
        return LoginResult(
            authTokens = authTokens,
            user = user,
        )
    }

    private fun doLoginWithSignUp(kakaoRefreshToken: String, kakaoSyncUserId: String): LoginResult {
        val user = userApplicationService.signUp(
            params = UserRepository.CreateUserParams(
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
                authProviderType = UserAuthProviderType.KAKAO,
                externalId = kakaoSyncUserId,
                externalRefreshToken = kakaoRefreshToken,
                externalRefreshTokenExpiresAt = SccClock.instant() + kakaoRefreshTokenExpirationDuration,
            )
        )

        val authTokens = userAuthService.issueTokens(newUserAuthInfo)
        return LoginResult(
            authTokens = authTokens,
            user = user,
        )
    }

    companion object {
        private val kakaoRefreshTokenExpirationDuration = Duration.ofDays(30)
    }
}
