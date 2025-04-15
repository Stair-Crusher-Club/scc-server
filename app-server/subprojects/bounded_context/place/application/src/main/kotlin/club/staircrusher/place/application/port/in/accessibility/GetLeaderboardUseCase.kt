package club.staircrusher.place.application.port.`in`.accessibility

import club.staircrusher.place.application.port.`in`.accessibility.result.WithUserInfo
import club.staircrusher.place.application.port.out.accessibility.persistence.AccessibilityRankRepository
import club.staircrusher.place.application.result.toDomainModel
import club.staircrusher.place.domain.model.accessibility.AccessibilityRank
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
