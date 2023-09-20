package club.staircrusher.challenge.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.ChallengeDto
import club.staircrusher.api.spec.dto.ChallengeStatusDto
import club.staircrusher.api.spec.dto.GetChallengeRequestDto
import club.staircrusher.api.spec.dto.GetChallengeResponseDto
import club.staircrusher.api.spec.dto.JoinChallengeRequestDto
import club.staircrusher.api.spec.dto.JoinChallengeResponseDto
import club.staircrusher.api.spec.dto.ListChallengesItemDto
import club.staircrusher.api.spec.dto.ListChallengesRequestDto
import club.staircrusher.api.spec.dto.ListChallengesResponseDto
import club.staircrusher.challenge.application.port.`in`.ChallengeService
import club.staircrusher.challenge.infra.adapter.out.persistence.sqldelight.toDto
import club.staircrusher.challenge.infra.adapter.out.persistence.sqldelight.toListChallengeDto
import club.staircrusher.spring_web.security.app.SccAppAuthentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.Clock
import java.util.*

@RestController
class ChallengeController(
    private val challengeService: ChallengeService,
    private val clock: Clock
) {
    @PostMapping("/getChallenge")
    fun getChallenge(
        @RequestBody request: GetChallengeRequestDto,
        authentication: SccAppAuthentication?,
    ): GetChallengeResponseDto {
        return GetChallengeResponseDto(
            challenge = ChallengeDto(
                id = null,
                name = null,
                isPublic = null,
                isComplete = null,
                startsAt = null,
                endsAt = null,
                goals = listOf(),
                conditions = listOf(),
                createdAt = null
            ),
            ranks = listOf(),
            hasJoined = false,
            myRank = null
        )
    }

    @PostMapping("/joinChallenge")
    fun joinChallenge(
        @RequestBody request: JoinChallengeRequestDto,
        authentication: SccAppAuthentication,
    ): JoinChallengeResponseDto {
        val userId = authentication.principal
        val joinedChallenge = challengeService.joinChallenge(userId = userId, challengeId = request.challengeId, passcode = request.passcode)
        return JoinChallengeResponseDto(
            challenge = joinedChallenge.toDto(criteriaTime = clock.instant()),
            ranks = listOf()
        )
    }

    @PostMapping("/listChallenges")
    fun listChallenges(
        @RequestBody request: ListChallengesRequestDto,
        authentication: SccAppAuthentication?,
    ): ListChallengesResponseDto {
        val result = request.status
            .flatMap { status ->
                return@flatMap when (status) {
                    ChallengeStatusDto.inProgress -> {
                        authentication?.principal?.let { userId ->
                            challengeService.getInProgressChallenges(
                                ChallengeService.MyChallengeOption.Only(userId = userId)
                            )
                                .map { it.toListChallengeDto(hasJoined = true, criteriaTime = clock.instant()) } +
                                challengeService.getInProgressChallenges(
                                    ChallengeService.MyChallengeOption.Without(userId = userId)
                                )
                                    .map { it.toListChallengeDto(hasJoined = false, criteriaTime = clock.instant()) }
                        }
                            ?: challengeService.getInProgressChallenges()
                                .map { it.toListChallengeDto(hasJoined = false, clock.instant()) }
                    }

                    ChallengeStatusDto.upcoming -> challengeService.getUpcomingChallenges()
                        .map { it.toListChallengeDto(hasJoined = false, criteriaTime = clock.instant()) }

                    ChallengeStatusDto.closed -> challengeService.getClosedChallenges()
                        .map { it.toListChallengeDto(hasJoined = false, criteriaTime = clock.instant()) }

                    else -> listOf<ListChallengesItemDto>()
                }
            }
        // TODO: 페이징 구현
        return ListChallengesResponseDto(
            totalCount = result.count().toLong(),
            items = result.take(request.limit?.toInt() ?: 128),
            nextToken = null,
        )
    }
}
