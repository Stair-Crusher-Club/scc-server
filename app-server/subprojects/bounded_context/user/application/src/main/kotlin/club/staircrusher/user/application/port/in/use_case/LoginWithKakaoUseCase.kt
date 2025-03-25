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

@Component
class LoginWithKakaoUseCase(
    private val transactionManager: TransactionManager,
    private val kakaoLoginService: KakaoLoginService,
    private val userProfileRepository: UserProfileRepository,
    private val userAuthInfoRepository: UserAuthInfoRepository,
    private val userAuthService: UserAuthService,
    private val userApplicationService: UserApplicationService,
) {
    fun handle(kakaoRefreshToken: String, rawKakaoIdToken: String, anonymousUserId: String?): LoginResult = transactionManager.doInTransaction {
        val idToken = kakaoLoginService.parseIdToken(rawKakaoIdToken)

        val userAuthInfo = userAuthInfoRepository.findFirstByAuthProviderTypeAndExternalId(UserAuthProviderType.KAKAO, idToken.kakaoSyncUserId)
        if (userAuthInfo != null) {
            doLoginForExistingUser(userAuthInfo,  kakaoRefreshToken, anonymousUserId)
        } else {
            doLoginWithSignUp(kakaoRefreshToken, idToken.kakaoSyncUserId, anonymousUserId)
        }
    }

    private fun doLoginForExistingUser(userAuthInfo: UserAuthInfo, kakaoRefreshToken: String, anonymousUserId: String?): LoginResult {
        userAuthInfo.externalRefreshToken = kakaoRefreshToken
        // 처음 expiresAt 을 세팅할 때 kakao 에서 주는 expiresAt 을 사용하지 않았기 때문에 로그인 시 결과로 나온 refresh token 이 언제 만료되는지 알 수 없다
        // 그래서 일단 1일로 세팅하고 나중에 자동 refresh token 잡이 처리하도록 한다
        userAuthInfo.externalRefreshTokenExpiresAt = SccClock.instant() + Duration.ofDays(1L)
        userAuthInfoRepository.save(userAuthInfo)

        val authTokens = userAuthService.issueTokens(userAuthInfo)
        val userProfile = userProfileRepository.findFirstByUserId(userAuthInfo.userId) ?: throw SccDomainException("계정 정보를 찾을 수 없습니다")
        anonymousUserId?.let { userApplicationService.connectToIdentifiedAccount(it, userAuthInfo.userId, UserConnectionReason.LOGIN) }

        return LoginResult(
            authTokens = authTokens,
            userProfile = userProfile,
        )
    }

    private fun doLoginWithSignUp(kakaoRefreshToken: String, kakaoSyncUserId: String, anonymousUserId: String?): LoginResult {
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
                externalRefreshTokenExpiresAt = SccClock.instant() + kakaoRefreshTokenExpirationDuration,
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
