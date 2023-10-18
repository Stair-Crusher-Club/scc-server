package club.staircrusher.challenge.application.port.`in`.use_case

import club.staircrusher.challenge.application.port.`in`.ChallengeService
import club.staircrusher.challenge.domain.model.Challenge
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager
import java.time.Clock
import java.time.Instant

@Component
class ListChallengesUseCase(
    private val transactionManager: TransactionManager,
    private val challengeService: ChallengeService,
    private val clock: Clock
) {
    enum class Status {
        IN_PROGRESS,
        UPCOMING,
        CLOSED
    }

    data class ListChallengesItem(
        val challenge: Challenge,
        val hasJoined: Boolean = false
    )

    fun handle(
        userId: String?,
        statuses: List<Status>?,
        criteriaTime: Instant = clock.instant()
    ): List<ListChallengesItem> =
        transactionManager.doInTransaction {
            val targetStatues = statuses ?: Status.values().toList()
            return@doInTransaction targetStatues.flatMap { s ->
                when (s) {
                    Status.IN_PROGRESS -> {
                        val myInProgressChallenges =
                            userId?.let { challengeService.getMyInProgressChallenges(it, criteriaTime) } ?: listOf()
                        val notMyInProgressChallenges = challengeService.getInProgressChallenges(criteriaTime)
                            .filter { myInProgressChallenges.contains(it).not() }
                        return@flatMap myInProgressChallenges
                            .map { ListChallengesItem(challenge = it, hasJoined = true) } +
                            notMyInProgressChallenges
                                .map { ListChallengesItem(challenge = it) }
                    }

                    Status.UPCOMING -> challengeService.getUpcomingChallenges(criteriaTime)
                        .map { ListChallengesItem(challenge = it) }

                    Status.CLOSED -> challengeService.getClosedChallenges(criteriaTime)
                        .map { ListChallengesItem(challenge = it) }
                }
            }
        }
}
