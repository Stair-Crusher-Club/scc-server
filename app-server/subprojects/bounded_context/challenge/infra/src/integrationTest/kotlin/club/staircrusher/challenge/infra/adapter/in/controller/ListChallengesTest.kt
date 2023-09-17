package club.staircrusher.challenge.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.ChallengeStatusDto
import club.staircrusher.api.spec.dto.ListChallengesRequestDto
import club.staircrusher.api.spec.dto.ListChallengesResponseDto
import club.staircrusher.challenge.application.port.out.persistence.ChallengeContributionRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeParticipationRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeRepository
import club.staircrusher.challenge.infra.adapter.`in`.controller.base.ChallengeITBase
import org.junit.jupiter.api.Assertions.assertEquals
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

        registerChallenges()
    }

    @Test
    fun `진행 중 & 참여 중, 진행 중, 오픈 예정, 종료 순 으로 내려준다`() {
        val user = transactionManager.doInTransaction { testDataGenerator.createUser() }
        val challenges = mvc
            .sccRequest(
                "/listChallenges",
                ListChallengesRequestDto(
                    status = listOf(ChallengeStatusDto.inProgress, ChallengeStatusDto.upcoming, ChallengeStatusDto.closed),
                    nextToken = null,
                    limit = null
                ),
                user = user
            )
            .getResult(ListChallengesResponseDto::class)
            .items
        val sortedChallenges = challenges.sortedWith { c1, c2 ->
            val priority = listOf(
                ChallengeStatusDto.closed,
                ChallengeStatusDto.upcoming,
                ChallengeStatusDto.inProgress
            )
            return@sortedWith when {
                priority.indexOf(c1.status) > priority.indexOf(c2.status) -> 1
                priority.indexOf(c1.status) < priority.indexOf(c2.status) -> 1
                // 그 외 진행 중 / 오픈 예정 / 종료 순 챌린지는 생성순으로 내려준다.
                else -> when {
                    c1.createdAt.value > c2.createdAt.value -> 1
                    c1.createdAt.value < c2.createdAt.value -> -1
                    else -> 0
                }
            }
        }
        assertEquals(sortedChallenges, challenges)
    }

    @Test
    fun `필터가 있는 경우 그에 해당하는 챌린지만 내려온다`() {
        val inProgressChallenges = mvc
            .sccRequest(
                "/listChallenges",
                ListChallengesRequestDto(
                    status = listOf(ChallengeStatusDto.inProgress),
                    nextToken = null,
                    limit = null
                )
            )
            .getResult(ListChallengesResponseDto::class)
            .items
        assert(
            inProgressChallenges
                .map { it.status }
                .all { it == ChallengeStatusDto.inProgress }
        )

        val upcomingChallenges = mvc
            .sccRequest(
                "/listChallenges",
                ListChallengesRequestDto(
                    status = listOf(ChallengeStatusDto.upcoming),
                    nextToken = null,
                    limit = null
                )
            )
            .getResult(ListChallengesResponseDto::class)
            .items
        assert(
            upcomingChallenges
                .map { it.status }
                .all { it == ChallengeStatusDto.upcoming }
        )

        val closedChallenges = mvc
            .sccRequest(
                "/listChallenges",
                ListChallengesRequestDto(
                    status = listOf(ChallengeStatusDto.closed),
                    nextToken = null,
                    limit = null
                )
            )
            .getResult(ListChallengesResponseDto::class)
            .items
        assert(
            closedChallenges
                .map { it.status }
                .all { it == ChallengeStatusDto.closed }
        )
    }

    @Test
    fun `필터가 여러 개 있는 경우 여러 필터에 해당하는 챌린지만 내려온다`() {
        val inProgressOrUpcomingChallenges = mvc
            .sccRequest(
                "/listChallenges",
                ListChallengesRequestDto(
                    status = listOf(ChallengeStatusDto.inProgress, ChallengeStatusDto.upcoming),
                    nextToken = null,
                    limit = null
                )
            )
            .getResult(ListChallengesResponseDto::class)
            .items
        assert(
            inProgressOrUpcomingChallenges
                .map { it.status }
                .all { it == ChallengeStatusDto.inProgress || it == ChallengeStatusDto.upcoming }
        )
        val upcomingOrCloseChallenges = mvc
            .sccRequest(
                "/listChallenges",
                ListChallengesRequestDto(
                    status = listOf(ChallengeStatusDto.upcoming, ChallengeStatusDto.closed),
                    nextToken = null,
                    limit = null
                )
            )
            .getResult(ListChallengesResponseDto::class)
            .items
        assert(
            upcomingOrCloseChallenges
                .map { it.status }
                .all { it == ChallengeStatusDto.upcoming || it == ChallengeStatusDto.closed }
        )
    }

    @Test
    fun `진행 중 & 참여 중 챌린지는 앞쪽에 참여한 순서대로 차례대로 나온다`() {
        val inProgressChallengesBeforeParticipation = mvc
            .sccRequest(
                "/listChallenges",
                ListChallengesRequestDto(
                    status = listOf(ChallengeStatusDto.inProgress),
                    nextToken = null,
                    limit = null
                )
            )
            .getResult(ListChallengesResponseDto::class)
            .items
        val firstChallengeBeforeParticipation = inProgressChallengesBeforeParticipation.first()
        assert(firstChallengeBeforeParticipation.status == ChallengeStatusDto.inProgress)
        assert(firstChallengeBeforeParticipation.hasJoined.not())
        val lastChallengeBeforeParticipation = inProgressChallengesBeforeParticipation.last()
        assert(lastChallengeBeforeParticipation.status == ChallengeStatusDto.inProgress)
        assert(lastChallengeBeforeParticipation.hasJoined.not())

        val user = transactionManager.doInTransaction { testDataGenerator.createUser() }
        participate(
            user = user,
            challenge = challengeRepository.findById(lastChallengeBeforeParticipation.id),
        )
        participate(
            user = user,
            challenge = challengeRepository.findById(firstChallengeBeforeParticipation.id),
        )
        val challengesAfterParticipation = mvc
            .sccRequest(
                "/listChallenges",
                ListChallengesRequestDto(
                    status = listOf(ChallengeStatusDto.inProgress, ChallengeStatusDto.upcoming, ChallengeStatusDto.closed),
                    nextToken = null,
                    limit = null
                ),
                user = user
            )
            .getResult(ListChallengesResponseDto::class)
            .items
        val firstChallenge = challengesAfterParticipation.getOrNull(0)
        assert(firstChallenge != null)
        assert(firstChallenge?.id == lastChallengeBeforeParticipation.id)
        assert(firstChallenge?.status == ChallengeStatusDto.inProgress)
        assert(firstChallenge?.hasJoined == true)
        val secondChallenge = challengesAfterParticipation.getOrNull(1)
        assert(secondChallenge != null)
        assert(secondChallenge?.id == firstChallengeBeforeParticipation.id)
        assert(secondChallenge?.status == ChallengeStatusDto.inProgress)
        assert(secondChallenge?.hasJoined == true)
        val thirdChallenge = challengesAfterParticipation.getOrNull(2)
        assert(thirdChallenge != null)
        assert(thirdChallenge?.status == ChallengeStatusDto.inProgress)
        assert(thirdChallenge?.hasJoined == false)
    }

    // TODO: 페이징 때 쓸 테스트코드
//    @Test
//    fun `페이징`() {
//        val challenges = mvc
//            .sccRequest(
//                "/listChallenges",
//                ListChallengesRequestDto(
//                    status = listOf(ChallengeStatusDto.inProgress, ChallengeStatusDto.upcoming, ChallengeStatusDto.closed),
//                    nextToken = null,
//                    limit = 9
//                )
//            )
//            .getResult(ListChallengesResponseDto::class)
//            .items
//        val firstPage = mvc
//            .sccRequest(
//                "/listChallenges",
//                ListChallengesRequestDto(
//                    status = listOf(ChallengeStatusDto.inProgress, ChallengeStatusDto.upcoming, ChallengeStatusDto.closed),
//                    nextToken = null,
//                    limit = 3
//                )
//            )
//            .getResult(ListChallengesResponseDto::class)
//        val secondPage = mvc
//            .sccRequest(
//                "/listChallenges",
//                ListChallengesRequestDto(
//                    status = listOf(ChallengeStatusDto.inProgress, ChallengeStatusDto.upcoming, ChallengeStatusDto.closed),
//                    nextToken = firstPage.nextToken,
//                    limit = 3
//                )
//            )
//            .getResult(ListChallengesResponseDto::class)
//        val thirdPage = mvc
//            .sccRequest(
//                "/listChallenges",
//                ListChallengesRequestDto(
//                    status = listOf(ChallengeStatusDto.inProgress, ChallengeStatusDto.upcoming, ChallengeStatusDto.closed),
//                    nextToken = secondPage.nextToken,
//                    limit = 3
//                )
//            )
//            .getResult(ListChallengesResponseDto::class)
//        assertEquals(challenges, firstPage.items + secondPage.items + thirdPage.items)
//    }
}
