package club.staircrusher.place.application.port.`in`.accessibility

import club.staircrusher.place.application.port.`in`.accessibility.result.WithUserInfo
import club.staircrusher.place.application.port.out.accessibility.persistence.AccessibilityRankRepository
import club.staircrusher.place.application.result.toDomainModel
import club.staircrusher.place.domain.model.accessibility.AccessibilityRank
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.user.application.port.`in`.UserApplicationService
import java.util.UUID

@Component
class GetRankUseCase(
    private val accessibilityRankRepository: AccessibilityRankRepository,
    private val userApplicationService: UserApplicationService,
) {
    fun handle(userId: String): WithUserInfo<AccessibilityRank> {
        val now = SccClock.instant()
        val userProfile = userApplicationService.getProfileByUserIdOrNull(userId) ?: throw SccDomainException("잘못된 계정입니다.")
        val accessibilityRank = accessibilityRankRepository.findFirstByUserId(userId) ?: run {
            // if lastRank can not be found, then the user is the first rank
            val lastRank = accessibilityRankRepository.findRankByConqueredCount(0) ?: 1
            AccessibilityRank(
                id = UUID.randomUUID().toString(),
                userId = userProfile.userId,
                conqueredCount = 0,
                rank = lastRank,
                createdAt = now,
                updatedAt = now,
            )
        }

        return WithUserInfo(accessibilityRank, userProfile.toDomainModel())
    }
}
