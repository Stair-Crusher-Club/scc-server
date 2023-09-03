package club.staircrusher.challenge.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.ChallengeDto
import club.staircrusher.api.spec.dto.GetChallengeRequestDto
import club.staircrusher.api.spec.dto.GetChallengeResponseDto
import club.staircrusher.api.spec.dto.JoinChallengeRequestDto
import club.staircrusher.api.spec.dto.JoinChallengeResponseDto
import club.staircrusher.api.spec.dto.ListChallengesRequestDto
import club.staircrusher.api.spec.dto.ListChallengesResponseDto
import club.staircrusher.spring_web.security.app.SccAppAuthentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ChallengeController(
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
    fun joinChallenge(@RequestBody request: JoinChallengeRequestDto): JoinChallengeResponseDto {
        return JoinChallengeResponseDto(
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
            hasJoined = false
        )
    }

    @PostMapping("/listChallenges")
    fun listChallenges(@RequestBody request: ListChallengesRequestDto): ListChallengesResponseDto {
        return ListChallengesResponseDto(items = listOf())
    }
}
