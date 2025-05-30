package club.staircrusher.user.application.port.`in`.use_case

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.application.port.`in`.InitialNicknameGenerator
import club.staircrusher.user.application.port.`in`.UserApplicationService
import club.staircrusher.user.application.port.out.persistence.UserAuthInfoRepository
import club.staircrusher.user.application.port.out.persistence.UserProfileRepository
import club.staircrusher.user.application.port.out.web.login.kakao.KakaoLoginService
import club.staircrusher.user.domain.model.UserAuthInfo
import club.staircrusher.user.domain.model.UserAuthProviderType
import club.staircrusher.user.domain.model.UserConnectionReason
import club.staircrusher.user.domain.service.UserAuthService
import java.time.Duration
import java.time.Instant

@Component
class LoginWithKakaoUseCase(
    private val transactionManager: TransactionManager,
    private val kakaoLoginService: KakaoLoginService,
    private val userProfileRepository: UserProfileRepository,
    private val userAuthInfoRepository: UserAuthInfoRepository,
    private val userAuthService: UserAuthService,
    private val userApplicationService: UserApplicationService,
) {
    fun handle(
        kakaoRefreshToken: String,
        rawKakaoIdToken: String,
        refreshTokenExpiresAt: Instant?,
        anonymousUserId: String?,
    ): LoginResult = transactionManager.doInTransaction {
        val idToken = kakaoLoginService.parseIdToken(rawKakaoIdToken)

        val userAuthInfo = userAuthInfoRepository.findFirstByAuthProviderTypeAndExternalId(UserAuthProviderType.KAKAO, idToken.kakaoSyncUserId)
        if (userAuthInfo != null) {
            doLoginForExistingUser(userAuthInfo, kakaoRefreshToken, refreshTokenExpiresAt, anonymousUserId)
        } else {
            doLoginWithSignUp(kakaoRefreshToken, idToken.kakaoSyncUserId, refreshTokenExpiresAt, anonymousUserId)
        }
    }

    private fun doLoginForExistingUser(userAuthInfo: UserAuthInfo, kakaoRefreshToken: String, refreshTokenExpiresAt: Instant?, anonymousUserId: String?): LoginResult {
        userAuthInfo.externalRefreshToken = kakaoRefreshToken
        userAuthInfo.externalRefreshTokenExpiresAt = refreshTokenExpiresAt ?: (SccClock.instant() + kakaoRefreshTokenExpirationDuration)
        userAuthInfoRepository.save(userAuthInfo)

        val authTokens = userAuthService.issueTokens(userAuthInfo)
        val userProfile = userProfileRepository.findFirstByUserId(userAuthInfo.userId) ?: throw SccDomainException("계정 정보를 찾을 수 없습니다")
        anonymousUserId?.let { userApplicationService.connectToIdentifiedAccount(it, userAuthInfo.userId, UserConnectionReason.LOGIN) }

        return LoginResult(
            authTokens = authTokens,
            userProfile = userProfile,
        )
    }

    private fun doLoginWithSignUp(kakaoRefreshToken: String, kakaoSyncUserId: String, refreshTokenExpiresAt: Instant?, anonymousUserId: String?): LoginResult {
        val (user, userProfile) = userApplicationService.signUp(
            params = UserProfileRepository.CreateUserParams(
                nickname = InitialNicknameGenerator.generate(),
                password = null,
                instagramId = null,
                email = null,
                birthYear = null,
            )
        )
        anonymousUserId?.let { userApplicationService.connectToIdentifiedAccount(it, user.id, UserConnectionReason.SIGN_UP) }

        val newUserAuthInfo = userAuthInfoRepository.save(
            UserAuthInfo(
                id = EntityIdGenerator.generateRandom(),
                userId = user.id,
                authProviderType = UserAuthProviderType.KAKAO,
                externalId = kakaoSyncUserId,
                externalRefreshToken = kakaoRefreshToken,
                externalRefreshTokenExpiresAt = refreshTokenExpiresAt ?: (SccClock.instant() + kakaoRefreshTokenExpirationDuration),
            )
        )

        val authTokens = userAuthService.issueTokens(newUserAuthInfo)
        return LoginResult(
            authTokens = authTokens,
            userProfile = userProfile,
        )
    }

    companion object {
        val kakaoRefreshTokenExpirationDuration = Duration.ofDays(30)
    }
}
