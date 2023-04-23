package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.persistence.AccessibilityRankRepository
import club.staircrusher.accessibility.domain.model.AccessibilityRank
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException

@Component
class GetMyRankUseCase(
    private val accessibilityRankRepository: AccessibilityRankRepository,
) {
    fun handle(userId: String): AccessibilityRank {
        return accessibilityRankRepository.findByUserId(userId) ?: throw SccDomainException("잘못된 계정입니다.")
    }
}
