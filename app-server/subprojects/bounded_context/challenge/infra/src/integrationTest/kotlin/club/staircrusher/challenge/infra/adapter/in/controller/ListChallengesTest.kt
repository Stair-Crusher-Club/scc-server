package club.staircrusher.challenge.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.ChallengeStatusDto
import club.staircrusher.api.spec.dto.ListChallengesRequestDto
import club.staircrusher.api.spec.dto.ListChallengesResponseDto
import club.staircrusher.challenge.application.port.out.persistence.ChallengeContributionRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeParticipationRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeRepository
import club.staircrusher.challenge.infra.adapter.`in`.controller.base.ChallengeITBase
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ListChallengesTest : ChallengeITBase() {
    @Autowired
    private lateinit var challengeRepository: ChallengeRepository

    @Autowired
    private lateinit var challengeContributionRepository: ChallengeContributionRepository

    @Autowired
    private lateinit var challengeParticipationRepository: ChallengeParticipationRepository

    @BeforeEach
    fun setUp() = transactionManager.doInTransaction {
        challengeRepository.removeAll()
    }

    @Test
    fun `진행 중 & 참여 중, 진행 중, 오픈 예정, 종료 순 으로 내려준다`() {
        mvc
            .sccRequest(
                "/listChallenges",
                ListChallengesRequestDto(
                    status = listOf(),
                    nextToken = null,
                    limit = null
                )
            )
            .apply {
                val result = getResult(ListChallengesResponseDto::class)
                Assertions.assertEquals(10, result.items.size)
                // 진행 중 & 참여 중 인 챌린지는 가장 최근에 참여한 순서대로 내려준다.
                // 그 외 진행 중 / 오픈 예정 / 종료 순 챌린지는 생성순으로 내려준다.
            }
    }

    @Test
    fun `필터가 있는 경우 그에 해당하는 챌린지만 내려온다`() {
        mvc
            .sccRequest(
                "/listChallenges",
                ListChallengesRequestDto(
                    status = listOf(ChallengeStatusDto.inProgress),
                    nextToken = null,
                    limit = null
                )
            )
            .getResult(ListChallengesResponseDto::class)
        mvc
            .sccRequest(
                "/listChallenges",
                ListChallengesRequestDto(
                    status = listOf(ChallengeStatusDto.upcoming),
                    nextToken = null,
                    limit = null
                )
            )
            .getResult(ListChallengesResponseDto::class)
        mvc
            .sccRequest(
                "/listChallenges",
                ListChallengesRequestDto(
                    status = listOf(ChallengeStatusDto.closed),
                    nextToken = null,
                    limit = null
                )
            )
            .getResult(ListChallengesResponseDto::class)
    }

    @Test
    fun `필터가 여러 개 있는 경우 여러 필터에 해당하는 챌린지만 내려온다`() {
        mvc
            .sccRequest(
                "/listChallenges",
                ListChallengesRequestDto(
                    status = listOf(ChallengeStatusDto.inProgress, ChallengeStatusDto.upcoming),
                    nextToken = null,
                    limit = null
                )
            )
            .getResult(ListChallengesResponseDto::class)
        mvc
            .sccRequest(
                "/listChallenges",
                ListChallengesRequestDto(
                    status = listOf(ChallengeStatusDto.upcoming, ChallengeStatusDto.closed),
                    nextToken = null,
                    limit = null
                )
            )
            .getResult(ListChallengesResponseDto::class)
    }

    @Test
    fun `진행 중 & 참여 중 챌린지는 앞쪽에 참여한 순서대로 차례대로 나온다`() {
        mvc
            .sccRequest(
                "/listChallenges",
                ListChallengesRequestDto(
                    status = listOf(),
                    nextToken = null,
                    limit = null
                )
            )
            .getResult(ListChallengesResponseDto::class)
    }
}
