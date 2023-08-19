package club.staircrusher.user.application.port.`in`.use_case

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.application.port.`in`.dto.LoginResult
import club.staircrusher.user.application.port.`in`.exception.SignUpRequiredException
import club.staircrusher.user.application.port.out.persistence.UserAuthInfoRepository
import club.staircrusher.user.application.port.out.web.KakaoLoginService
import club.staircrusher.user.domain.model.UserAuthProviderType
import club.staircrusher.user.domain.service.UserAuthService

@Component
class LoginWithKakaoUseCase(
    private val transactionManager: TransactionManager,
    private val kakaoLoginService: KakaoLoginService,
    private val userAuthInfoRepository: UserAuthInfoRepository,
    private val userAuthService: UserAuthService,
) {
    fun handle(rawKakaoIdToken: String): LoginResult = transactionManager.doInTransaction {
        val idToken = kakaoLoginService.parseIdToken(rawKakaoIdToken)

        val userAuthInfo = userAuthInfoRepository.findByExternalId(UserAuthProviderType.KAKAO, idToken.kakaoSyncUserId)
            ?: throw SignUpRequiredException("UserAuthInfo does not exists.")

        val accessToken = userAuthService.issueAccessToken(userAuthInfo)
        LoginResult(accessToken = accessToken)
    }
}
