package club.staircrusher.user.application.port.`in`.use_case

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.application.port.`in`.UserApplicationService
import club.staircrusher.user.domain.model.LoginResult
import club.staircrusher.user.application.port.out.persistence.UserAuthInfoRepository
import club.staircrusher.user.application.port.out.persistence.UserRepository
import club.staircrusher.user.application.port.out.web.KakaoLoginService
import club.staircrusher.user.domain.model.UserAuthInfo
import club.staircrusher.user.domain.model.UserAuthProviderType
import club.staircrusher.user.domain.service.UserAuthService
import java.time.Duration

@Component
class SignUpWithKakaoUseCase(
    private val transactionManager: TransactionManager,
    private val kakaoLoginService: KakaoLoginService,
    private val userApplicationService: UserApplicationService,
    private val userAuthInfoRepository: UserAuthInfoRepository,
    private val userAuthService: UserAuthService,
) {
    fun handle(
        nickname: String,
        email: String,
        instagramId: String?,
        kakaoRefreshToken: String,
        rawKakaoIdToken: String,
    ): LoginResult = transactionManager.doInTransaction {
        val idToken = kakaoLoginService.parseIdToken(rawKakaoIdToken)

        val createUserParams = UserRepository.CreateUserParams(
            nickname = nickname,
            password = null,
            instagramId = instagramId,
            email = email,
        )

        val user = userApplicationService.signUp(createUserParams)

        val userAuthInfo = userAuthInfoRepository.save(
            UserAuthInfo(
                id = EntityIdGenerator.generateRandom(),
                userId = user.id,
                authProviderType = UserAuthProviderType.KAKAO,
                externalId = idToken.kakaoSyncUserId,
                externalRefreshToken = kakaoRefreshToken,
                externalRefreshTokenExpiresAt = SccClock.instant() + kakaoRefreshTokenExpirationDuration,
            )
        )

        userAuthService.issueTokens(userAuthInfo)
    }

    companion object {
        private val kakaoRefreshTokenExpirationDuration = Duration.ofDays(30)
    }
}
