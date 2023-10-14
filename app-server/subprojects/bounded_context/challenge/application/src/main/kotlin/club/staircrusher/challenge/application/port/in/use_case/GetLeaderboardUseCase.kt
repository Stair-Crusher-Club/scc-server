package club.staircrusher.challenge.application.port.`in`.use_case

import club.staircrusher.stdlib.di.annotation.Component

/**
 * Get the leaderboard of a challenge which shows only the top 10 users.
 * If there are more than 2 users with the same score, those users' ranks
 * are the same and the next rank is skipped. For example, if there are
 * 3 users with the same score, the ranks are 1, 1, 1, 4, 5, 6, 7, 8, 9, 10.
 */
@Component
class GetLeaderboardUseCase {
    fun handle() {

    }
}
