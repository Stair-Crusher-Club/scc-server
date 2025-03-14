package club.staircrusher.user.application.port.`in`.use_case

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.stdlib.time.toEndOfDay
import club.staircrusher.stdlib.time.toStartOfDay
import club.staircrusher.user.application.port.out.persistence.UserAuthInfoRepository
import club.staircrusher.user.application.port.out.web.login.kakao.KakaoLoginService
import club.staircrusher.user.domain.model.UserAuthProviderType
import com.google.common.util.concurrent.RateLimiter
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import java.time.Duration

@Component
class UpdateExternalRefreshTokensUseCase(
    private val transactionManager: TransactionManager,
    private val userAuthInfoRepository: UserAuthInfoRepository,
    private val kakaoLoginService: KakaoLoginService
) {
    private val logger = KotlinLogging.logger {}

    @Suppress("UnstableApiUsage", "MagicNumber")
    private val rateLimiter = RateLimiter.create(10.0)

    fun handle() {
        logger.info { "UpdateExternalRefreshToken Job started" }

        // 카카오의 refresh 토큰은 2달간 유효하지만, LoginWithKakaoUseCase 에서 externalRefreshTokenExpiresAt 을 임의로 30일 뒤로 저장한다
        // 토큰 갱신하기 API 의 경우 refresh token 의 유효기간이 1개월 미만으로 남았을 때만 갱신되어 전달되므로
        // externalRefreshTokenExpiresAt 이 만료될 때쯤 요청을 보내서 refresh token 이 응답이 오는 경우에만 갱신해주면 된다.
        val from = SccClock.instant().toStartOfDay() - Duration.ofDays(1L)
        val to = SccClock.instant().toEndOfDay() + Duration.ofDays(1L)
        val userAuthIdToExpiringKakaoAuthTokens = transactionManager.doInTransaction(isReadOnly = true) {
            userAuthInfoRepository.findByAuthProviderTypeAndExternalRefreshTokenExpiresAtBetween(UserAuthProviderType.KAKAO, from, to)
                .map { it.id to it.externalRefreshToken }
        }

        val updatedTokens = userAuthIdToExpiringKakaoAuthTokens.mapNotNull { (id, token) ->
            rateLimiter.acquire()

            val kakaoLoginTokens = runBlocking { kakaoLoginService.refreshToken(token) } ?: return@mapNotNull null

            // refresh token 이 업데이트가 가능할 때만 response 에 refresh token 이 포함된다
            if (kakaoLoginTokens.refreshToken != null) {
                transactionManager.doInTransaction {
                    val userAuthInfo = userAuthInfoRepository.findByIdOrNull(id)!!
                    userAuthInfo.externalRefreshToken = kakaoLoginTokens.refreshToken
                    userAuthInfo.externalRefreshTokenExpiresAt = SccClock.instant() + LoginWithKakaoUseCase.kakaoRefreshTokenExpirationDuration
                }
            }
            kakaoLoginTokens.refreshToken
        }

        logger.info { "UpdateExternalRefresh Job complete. ${updatedTokens.size} tokens updated"}
    }
}
