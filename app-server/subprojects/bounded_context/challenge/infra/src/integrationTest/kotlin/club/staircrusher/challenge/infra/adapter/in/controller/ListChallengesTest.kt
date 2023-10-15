package club.staircrusher.challenge.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.ChallengeStatusDto
import club.staircrusher.api.spec.dto.ListChallengesRequestDto
import club.staircrusher.api.spec.dto.ListChallengesResponseDto
import club.staircrusher.challenge.application.port.out.persistence.ChallengeContributionRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeParticipationRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeRepository
import club.staircrusher.challenge.infra.adapter.`in`.controller.base.ChallengeITBase
import club.staircrusher.testing.spring_it.mock.MockSccClock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration

class ListChallengesTest : ChallengeITBase() {
    @Autowired
    private lateinit var challengeRepository: ChallengeRepository

    @Autowired
    private lateinit var challengeContributionRepository: ChallengeContributionRepository

    @Autowired
    private lateinit var challengeParticipationRepository: ChallengeParticipationRepository

    @Autowired
    private lateinit var clock: MockSccClock

    @BeforeEach
    fun setUp() = transactionManager.doInTransaction {
        challengeRepository.removeAll()
        challengeParticipationRepository.removeAll()

        registerChallenges()
    }

    @Test
    fun `진행 중 & 참여 중, 진행 중, 오픈 예정, 종료 순 으로 내려준다`() {
        val user = transactionManager.doInTransaction { testDataGenerator.createUser() }
        val challenges = mvc
            .sccRequest(
                "/listChallenges",
                ListChallengesRequestDto(
                    statuses = listOf(
                        ChallengeStatusDto.inProgress,
                        ChallengeStatusDto.upcoming,
                        ChallengeStatusDto.closed
                    ),
                    nextToken = null,
                    limit = null
                ),
                user = user
            )
            .getResult(ListChallengesResponseDto::class)
            .items
        val sortedChallenges = challenges.sortedWith { c1, c2 ->
            val priority = listOf(
                ChallengeStatusDto.inProgress,
                ChallengeStatusDto.upcoming,
                ChallengeStatusDto.closed,
            )
            return@sortedWith when {
                priority.indexOf(c1.status) > priority.indexOf(c2.status) -> 1
                priority.indexOf(c1.status) < priority.indexOf(c2.status) -> -1
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
                    statuses = listOf(ChallengeStatusDto.inProgress),
                    nextToken = null,
                    limit = null
                )
            )
            .getResult(ListChallengesResponseDto::class)
            .items
        assertTrue(
            inProgressChallenges
                .map { it.status }
                .all { it == ChallengeStatusDto.inProgress }
        )

        val upcomingChallenges = mvc
            .sccRequest(
                "/listChallenges",
                ListChallengesRequestDto(
                    statuses = listOf(ChallengeStatusDto.upcoming),
                    nextToken = null,
                    limit = null
                )
            )
            .getResult(ListChallengesResponseDto::class)
            .items
        assertTrue(
            upcomingChallenges
                .map { it.status }
                .all { it == ChallengeStatusDto.upcoming }
        )

        val closedChallenges = mvc
            .sccRequest(
                "/listChallenges",
                ListChallengesRequestDto(
                    statuses = listOf(ChallengeStatusDto.closed),
                    nextToken = null,
                    limit = null
                )
            )
            .getResult(ListChallengesResponseDto::class)
            .items
        assertTrue(
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
                    statuses = listOf(ChallengeStatusDto.inProgress, ChallengeStatusDto.upcoming),
                    nextToken = null,
                    limit = null
                )
            )
            .getResult(ListChallengesResponseDto::class)
            .items
        assertTrue(
            inProgressOrUpcomingChallenges
                .map { it.status }
                .all { it == ChallengeStatusDto.inProgress || it == ChallengeStatusDto.upcoming }
        )
        val upcomingOrCloseChallenges = mvc
            .sccRequest(
                "/listChallenges",
                ListChallengesRequestDto(
                    statuses = listOf(ChallengeStatusDto.upcoming, ChallengeStatusDto.closed),
                    nextToken = null,
                    limit = null
                )
            )
            .getResult(ListChallengesResponseDto::class)
            .items
        assertTrue(
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
                    statuses = listOf(ChallengeStatusDto.inProgress),
                    nextToken = null,
                    limit = null
                )
            )
            .getResult(ListChallengesResponseDto::class)
            .items
        val firstChallengeBeforeParticipation = inProgressChallengesBeforeParticipation.first()
        assertTrue(firstChallengeBeforeParticipation.status == ChallengeStatusDto.inProgress)
        assertTrue(firstChallengeBeforeParticipation.hasJoined.not())
        val lastChallengeBeforeParticipation = inProgressChallengesBeforeParticipation.last()
        assertTrue(lastChallengeBeforeParticipation.status == ChallengeStatusDto.inProgress)
        assertTrue(lastChallengeBeforeParticipation.hasJoined.not())

        val user = transactionManager.doInTransaction { testDataGenerator.createUser() }
        participate(
            user = user,
            challenge = challengeRepository.findById(lastChallengeBeforeParticipation.id),
        )
        clock.advanceTime(Duration.ofMinutes(1))
        participate(
            user = user,
            challenge = challengeRepository.findById(firstChallengeBeforeParticipation.id),
        )
        val challengesAfterParticipation = mvc
            .sccRequest(
                "/listChallenges",
                ListChallengesRequestDto(
                    statuses = listOf(
                        ChallengeStatusDto.inProgress,
                        ChallengeStatusDto.upcoming,
                        ChallengeStatusDto.closed
                    ),
                    nextToken = null,
                    limit = null
                ),
                user = user
            )
            .getResult(ListChallengesResponseDto::class)
            .items
        val firstChallenge = challengesAfterParticipation.getOrNull(0)
        assertTrue(firstChallenge != null)
        assertTrue(firstChallenge?.id == firstChallengeBeforeParticipation.id)
        assertTrue(firstChallenge?.status == ChallengeStatusDto.inProgress)
        assertTrue(firstChallenge?.hasJoined == true)
        val secondChallenge = challengesAfterParticipation.getOrNull(1)
        assertTrue(secondChallenge != null)
        assertTrue(secondChallenge?.id == lastChallengeBeforeParticipation.id)
        assertTrue(secondChallenge?.status == ChallengeStatusDto.inProgress)
        assertTrue(secondChallenge?.hasJoined == true)
        val thirdChallenge = challengesAfterParticipation.getOrNull(2)
        assertTrue(thirdChallenge != null)
        assertTrue(thirdChallenge?.status == ChallengeStatusDto.inProgress)
        assertTrue(thirdChallenge?.hasJoined == false)
    }

    // TODO: 페이징 때 쓸 테스트코드
//    @Test
//    fun `페이징`() {
//        val challenges = mvc
//            .sccRequest(
//                "/listChallenges",
//                ListChallengesRequestDto(
//                    statuses = listOf(ChallengeStatusDto.inProgress, ChallengeStatusDto.upcoming, ChallengeStatusDto.closed),
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
//                    statuses = listOf(ChallengeStatusDto.inProgress, ChallengeStatusDto.upcoming, ChallengeStatusDto.closed),
//                    nextToken = null,
//                    limit = 3
//                )
//            )
//            .getResult(ListChallengesResponseDto::class)
//        val secondPage = mvc
//            .sccRequest(
//                "/listChallenges",
//                ListChallengesRequestDto(
//                    statuses = listOf(ChallengeStatusDto.inProgress, ChallengeStatusDto.upcoming, ChallengeStatusDto.closed),
//                    nextToken = firstPage.nextToken,
//                    limit = 3
//                )
//            )
//            .getResult(ListChallengesResponseDto::class)
//        val thirdPage = mvc
//            .sccRequest(
//                "/listChallenges",
//                ListChallengesRequestDto(
//                    statuses = listOf(ChallengeStatusDto.inProgress, ChallengeStatusDto.upcoming, ChallengeStatusDto.closed),
//                    nextToken = secondPage.nextToken,
//                    limit = 3
//                )
//            )
//            .getResult(ListChallengesResponseDto::class)
//        assertEquals(challenges, firstPage.items + secondPage.items + thirdPage.items)
//    }
}
