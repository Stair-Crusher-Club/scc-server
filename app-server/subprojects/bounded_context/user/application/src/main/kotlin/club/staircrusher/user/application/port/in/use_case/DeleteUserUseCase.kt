package club.staircrusher.user.application.port.`in`.use_case

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.application.port.`in`.UserApplicationService
import club.staircrusher.user.application.port.out.persistence.UserAuthInfoRepository
import club.staircrusher.user.application.port.out.web.login.apple.AppleLoginService
import club.staircrusher.user.application.port.out.web.login.kakao.KakaoLoginService
import club.staircrusher.user.domain.model.UserAuthProviderType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mu.KotlinLogging

@Component
class DeleteUserUseCase(
    private val transactionManager: TransactionManager,
    private val userApplicationService: UserApplicationService,
    private val appleLoginService: AppleLoginService,
    private val kakaoLoginService: KakaoLoginService,
    private val userAuthInfoRepository: UserAuthInfoRepository,
) {
    private val logger = KotlinLogging.logger {}

    fun handle(userId: String) = transactionManager.doInTransaction(TransactionIsolationLevel.REPEATABLE_READ) {
        userApplicationService.deleteUser(userId)

        val userAuthInfo = userAuthInfoRepository.findByUserId(userId).maxByOrNull { it.createdAt }
        userAuthInfo?.let {
            revokeExternalConnectionAfterCommit(userId, it.externalId, it.externalRefreshToken, it.authProviderType)
            userAuthInfoRepository.removeByUserId(userId)
        }
    }

    private fun revokeExternalConnectionAfterCommit(userId: String, externalId: String, refreshToken: String, authProviderType: UserAuthProviderType) {
        transactionManager.doAfterCommit {
            CoroutineScope(Dispatchers.IO).launch {
                val revokeResult = when (authProviderType) {
                    UserAuthProviderType.APPLE -> appleLoginService.revoke(refreshToken)
                    UserAuthProviderType.KAKAO -> kakaoLoginService.disconnect(externalId)
                }

                if (!revokeResult) {
                    logger.error {
                        "Failed to revoke token for user($userId) with externalId($externalId) and provider(${authProviderType})"
                    }
                }
            }
        }
    }
}
