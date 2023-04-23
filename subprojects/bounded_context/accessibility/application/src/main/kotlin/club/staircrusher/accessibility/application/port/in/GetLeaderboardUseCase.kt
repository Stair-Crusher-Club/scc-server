package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.persistence.AccessibilityRankRepository
import club.staircrusher.accessibility.domain.model.AccessibilityRank
import club.staircrusher.stdlib.di.annotation.Component

@Component
class GetLeaderboardUseCase(
    private val accessibilityRankRepository: AccessibilityRankRepository,
) {
    companion object {
        const val NUMBER_OF_TOP_RANKER = 10
    }
    fun handle(): List<AccessibilityRank> {
        return accessibilityRankRepository.findTopNUsers(NUMBER_OF_TOP_RANKER)
    }
}
