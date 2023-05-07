package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.persistence.AccessibilityRankRepository
import club.staircrusher.accessibility.domain.model.AccessibilityRank
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
    fun handle(userId: String): AccessibilityRank {
        val accessibilityRank = accessibilityRankRepository.findByUserId(userId)
        val now = SccClock.instant()

        if (accessibilityRank == null) {
            val user = userApplicationService.getUser(userId) ?: throw SccDomainException("잘못된 계정입니다.")
            // if lastRank can not be found, then the user is the first rank
            val lastRank = accessibilityRankRepository.findByConqueredCount(0)?.rank ?: 1
            return AccessibilityRank(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                conqueredCount = 0,
                rank = lastRank,
                createdAt = now,
                updatedAt = now,
            )
        }

        return accessibilityRank
    }
}
