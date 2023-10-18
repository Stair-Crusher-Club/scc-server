package club.staircrusher.challenge.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.ChallengeRankDto
import club.staircrusher.api.spec.dto.ChallengeStatusDto
import club.staircrusher.api.spec.dto.GetChallengeRequestDto
import club.staircrusher.api.spec.dto.GetChallengeResponseDto
import club.staircrusher.api.spec.dto.GetChallengeWithInvitationCodeRequestDto
import club.staircrusher.api.spec.dto.JoinChallengeRequestDto
import club.staircrusher.api.spec.dto.JoinChallengeResponseDto
import club.staircrusher.api.spec.dto.ListChallengesRequestDto
import club.staircrusher.api.spec.dto.ListChallengesResponseDto
import club.staircrusher.challenge.application.port.`in`.use_case.GetChallengeUseCase
import club.staircrusher.challenge.application.port.`in`.use_case.GetChallengeWithInvitationCodeUseCase
import club.staircrusher.challenge.application.port.`in`.use_case.JoinChallengeUseCase
import club.staircrusher.challenge.application.port.`in`.use_case.ListChallengesUseCase
import club.staircrusher.challenge.infra.adapter.out.persistence.sqldelight.toDto
import club.staircrusher.challenge.infra.adapter.out.persistence.sqldelight.toListChallengeDto
import club.staircrusher.spring_web.security.app.SccAppAuthentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.Clock

@RestController
class ChallengeController(
    private val getChallengeUseCase: GetChallengeUseCase,
    private val getChallengeWithInvitationCodeUseCase: GetChallengeWithInvitationCodeUseCase,
    private val joinChallengeUseCase: JoinChallengeUseCase,
    private val listChallengesUseCase: ListChallengesUseCase,
    private val clock: Clock
) {
    @PostMapping("/getChallenge")
    fun getChallenge(
        @RequestBody request: GetChallengeRequestDto,
        authentication: SccAppAuthentication?,
    ): GetChallengeResponseDto {
        val result = getChallengeUseCase.handle(userId = authentication?.principal, challengeId = request.challengeId)
        return GetChallengeResponseDto(
            challenge = result.challenge.toDto(
                participationsCount = result.participationsCount,
                contributionsCount = result.contributionsCount,
                criteriaTime = clock.instant()
            ),
            ranks = listOf(),
            hasJoined = result.hasJoined,
            hasPasscode = result.challenge.passcode != null,
            // TODO: rank 반영
            myRank = if (result.hasJoined) ChallengeRankDto(rank = 0, contributionCount = 0, nickname = "") else null
        )
    }

    @PostMapping("/getChallengeWithInvitationCode")
    fun getChallengeWithInvitationCode(
        @RequestBody request: GetChallengeWithInvitationCodeRequestDto,
        authentication: SccAppAuthentication,
    ): GetChallengeResponseDto {
        val result = getChallengeWithInvitationCodeUseCase.handle(
            userId = authentication.principal,
            invitationCode = request.invitationCode
        )
        return GetChallengeResponseDto(
            challenge = result.challenge.toDto(
                participationsCount = result.participationsCount,
                contributionsCount = result.contributionsCount,
                criteriaTime = clock.instant()
            ),
            ranks = listOf(),
            hasJoined = result.hasJoined,
            hasPasscode = result.challenge.passcode != null,
            // TODO: rank 반영
            myRank = if (result.hasJoined) ChallengeRankDto(rank = 0, contributionCount = 0, nickname = "") else null
        )
    }

    @PostMapping("/joinChallenge")
    fun joinChallenge(
        @RequestBody request: JoinChallengeRequestDto,
        authentication: SccAppAuthentication,
    ): JoinChallengeResponseDto {
        val userId = authentication.principal
        val result = joinChallengeUseCase.handle(
            userId = userId,
            challengeId = request.challengeId,
            passcode = request.passcode
        )
        return JoinChallengeResponseDto(
            challenge = result.challenge.toDto(
                participationsCount = result.participationsCount,
                contributionsCount = result.contributionsCount,
                criteriaTime = clock.instant()
            ),
            ranks = listOf()
        )
    }

    @PostMapping("/listChallenges")
    fun listChallenges(
        @RequestBody request: ListChallengesRequestDto,
        authentication: SccAppAuthentication?,
    ): ListChallengesResponseDto {
        val requestTime = clock.instant()
        val result = listChallengesUseCase.handle(
            userId = authentication?.principal,
            statuses = request.statuses?.mapNotNull {
                when (it) {
                    ChallengeStatusDto.inProgress -> ListChallengesUseCase.Status.IN_PROGRESS
                    ChallengeStatusDto.upcoming -> ListChallengesUseCase.Status.UPCOMING
                    ChallengeStatusDto.closed -> ListChallengesUseCase.Status.CLOSED
                    else -> null
                }
            },
            criteriaTime = requestTime
        )
            .map { it.challenge.toListChallengeDto(hasJoined = it.hasJoined, criteriaTime = requestTime) }
        // TODO: 페이징 구현
        return ListChallengesResponseDto(
            totalCount = result.count().toLong(),
            items = result,
            nextToken = null,
        )
    }
}
