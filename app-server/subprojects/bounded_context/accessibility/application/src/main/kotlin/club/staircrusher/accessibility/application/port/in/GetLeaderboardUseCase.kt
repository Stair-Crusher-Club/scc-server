package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.`in`.result.WithUserInfo
import club.staircrusher.accessibility.application.port.out.persistence.AccessibilityRankRepository
import club.staircrusher.accessibility.application.toDomainModel
import club.staircrusher.accessibility.domain.model.AccessibilityRank
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.user.application.port.`in`.UserApplicationService

@Component
class GetLeaderboardUseCase(
    private val accessibilityRankRepository: AccessibilityRankRepository,
    private val userApplicationService: UserApplicationService,
) {
    companion object {
        const val NUMBER_OF_TOP_RANKER = 10
    }
    fun handle(): List<WithUserInfo<AccessibilityRank>> {
        val leaderboards = accessibilityRankRepository.findTopNUsers(NUMBER_OF_TOP_RANKER)
        val userIds = leaderboards.map { it.userId }
        val userProfiles = userApplicationService.getProfilesByUserIds(userIds)

        return leaderboards.map { rank ->
            WithUserInfo(rank, userProfiles.first { it.userId == rank.userId }.toDomainModel())
        }
    }
}
