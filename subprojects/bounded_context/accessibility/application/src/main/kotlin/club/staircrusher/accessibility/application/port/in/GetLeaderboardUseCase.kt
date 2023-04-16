package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.domain.model.AccessibilityRank
import club.staircrusher.stdlib.di.annotation.Component

@Component
class GetLeaderboardUseCase {
    fun handle(): List<AccessibilityRank> {
        TODO()
    }
}
