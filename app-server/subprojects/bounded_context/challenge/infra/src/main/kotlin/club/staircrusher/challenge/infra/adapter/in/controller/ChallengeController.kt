package club.staircrusher.challenge.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.ChallengeStatusDto
import club.staircrusher.api.spec.dto.GetChallengeRequestDto
import club.staircrusher.api.spec.dto.GetChallengeResponseDto
import club.staircrusher.api.spec.dto.GetChallengeWithInvitationCodeRequestDto
import club.staircrusher.api.spec.dto.JoinChallengeRequestDto
import club.staircrusher.api.spec.dto.JoinChallengeResponseDto
import club.staircrusher.api.spec.dto.ListChallengesRequestDto
import club.staircrusher.api.spec.dto.ListChallengesResponseDto
import club.staircrusher.challenge.application.port.`in`.use_case.GetChallengeLeaderboardUseCase
import club.staircrusher.challenge.application.port.`in`.use_case.GetChallengeRankUseCase
import club.staircrusher.challenge.application.port.`in`.use_case.GetChallengeUseCase
import club.staircrusher.challenge.application.port.`in`.use_case.GetChallengeWithInvitationCodeUseCase
import club.staircrusher.challenge.application.port.`in`.use_case.GetCountForNextChallengeRankUseCase
import club.staircrusher.challenge.application.port.`in`.use_case.JoinChallengeUseCase
import club.staircrusher.challenge.application.port.`in`.use_case.ListChallengesUseCase
import club.staircrusher.challenge.application.port.`in`.use_case.UpdateChallengeRanksUseCase
import club.staircrusher.challenge.infra.adapter.out.persistence.sqldelight.toDto
import club.staircrusher.challenge.infra.adapter.out.persistence.sqldelight.toListChallengeDto
import club.staircrusher.spring_web.security.app.SccAppAuthentication
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.web.util.matcher.IpAddressMatcher
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.Clock

@RestController
class ChallengeController(
    private val getChallengeUseCase: GetChallengeUseCase,
    private val getChallengeWithInvitationCodeUseCase: GetChallengeWithInvitationCodeUseCase,
    private val getChallengeRankUseCase: GetChallengeRankUseCase,
    private val getChallengeLeaderboardUseCase: GetChallengeLeaderboardUseCase,
    private val getContributionCountForNextChallengeRankUseCase: GetCountForNextChallengeRankUseCase,
    private val joinChallengeUseCase: JoinChallengeUseCase,
    private val listChallengesUseCase: ListChallengesUseCase,
    private val updateChallengeRanksUseCase: UpdateChallengeRanksUseCase,
    private val clock: Clock
) {
    @PostMapping("/getChallenge")
    fun getChallenge(
        @RequestBody request: GetChallengeRequestDto,
        authentication: SccAppAuthentication?,
    ): GetChallengeResponseDto {
        val result = getChallengeUseCase.handle(userId = authentication?.principal, challengeId = request.challengeId)
        val ranks = if (result.hasJoined && authentication != null) {
            getChallengeLeaderboardUseCase.handle(challengeId = request.challengeId)
        } else {
            emptyList()
        }
        val myRank = if (result.hasJoined && authentication != null) {
            getChallengeRankUseCase.handle(
                challengeId = result.challenge.id,
                userId = authentication.principal
            )
        } else {
            null
        }
        val contributionCountForNextRank = if (result.hasJoined && authentication != null) {
            getContributionCountForNextChallengeRankUseCase.handle(
                challengeId = result.challenge.id,
                userId = authentication.principal
            )
        } else {
            null
        }
        return GetChallengeResponseDto(
            challenge = result.challenge.toDto(
                participationsCount = result.participationsCount,
                contributionsCount = result.contributionsCount,
                criteriaTime = clock.instant()
            ),
            ranks = ranks.map { (rank, user) -> rank.toDto(user!!.nickname) },
            hasJoined = result.hasJoined,
            hasPasscode = result.challenge.passcode != null,
            myRank = myRank?.let { (rank, user) -> rank.toDto(user!!.nickname) },
            contributionCountForNextRank = contributionCountForNextRank,
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
        val ranks = getChallengeLeaderboardUseCase.handle(result.challenge.id)
        val myRank = if (result.hasJoined) {
            getChallengeRankUseCase.handle(
                challengeId = result.challenge.id,
                userId = authentication.principal
            )
        } else {
            null
        }
        val contributionCountForNextRank = if (result.hasJoined) {
            getContributionCountForNextChallengeRankUseCase.handle(
                challengeId = result.challenge.id,
                userId = authentication.principal
            )
        } else {
            null
        }
        return GetChallengeResponseDto(
            challenge = result.challenge.toDto(
                participationsCount = result.participationsCount,
                contributionsCount = result.contributionsCount,
                criteriaTime = clock.instant()
            ),
            ranks = ranks.map { (rank, user) -> rank.toDto(user!!.nickname) },
            hasJoined = result.hasJoined,
            hasPasscode = result.challenge.passcode != null,
            myRank = myRank?.let { (rank, user) -> rank.toDto(user!!.nickname) },
            contributionCountForNextRank = contributionCountForNextRank,
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
        val ranks = getChallengeLeaderboardUseCase.handle(challengeId = request.challengeId)
        return JoinChallengeResponseDto(
            challenge = result.challenge.toDto(
                participationsCount = result.participationsCount,
                contributionsCount = result.contributionsCount,
                criteriaTime = clock.instant()
            ),
            ranks = ranks.map { (rank, user) -> rank.toDto(user!!.nickname) },
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
                    ChallengeStatusDto.IN_PROGRESS -> ListChallengesUseCase.Status.IN_PROGRESS
                    ChallengeStatusDto.UPCOMING -> ListChallengesUseCase.Status.UPCOMING
                    ChallengeStatusDto.CLOSED -> ListChallengesUseCase.Status.CLOSED
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

    @PostMapping("/updateChallengeRanks")
    fun updateAccessibilityRanks(request: HttpServletRequest) {
        val clusterIpAddressMatcher = IpAddressMatcher("10.42.0.0/16")
        val localIpAddressMatcher = IpAddressMatcher("127.0.0.1/32")
        if (
            !clusterIpAddressMatcher.matches(request)
            && !localIpAddressMatcher.matches(request)
        ) {
            throw IllegalArgumentException("Unauthorized")
        }
        updateChallengeRanksUseCase.handle()
    }
}
