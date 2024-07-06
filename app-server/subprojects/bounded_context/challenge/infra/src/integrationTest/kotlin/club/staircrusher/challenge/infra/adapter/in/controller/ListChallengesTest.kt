package club.staircrusher.challenge.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.ChallengeStatusDto
import club.staircrusher.api.spec.dto.ListChallengesItemDto
import club.staircrusher.api.spec.dto.ListChallengesRequestDto
import club.staircrusher.api.spec.dto.ListChallengesResponseDto
import club.staircrusher.challenge.application.port.out.persistence.ChallengeContributionRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeParticipationRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeRepository
import club.staircrusher.challenge.domain.model.ChallengeStatus
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
    }

    @Test
    fun `진행 중 & 참여 중, 진행 중, 오픈 예정, 종료 순 으로 내려준다`() {
        val registeredChallengesByStatus = transactionManager.doInTransaction { registerChallenges() }

        val user = transactionManager.doInTransaction { testDataGenerator.createUser() }
        val challenges = mvc
            .sccRequest(
                "/listChallenges",
                ListChallengesRequestDto(
                    statuses = listOf(
                        ChallengeStatusDto.IN_PROGRESS,
                        ChallengeStatusDto.UPCOMING,
                        ChallengeStatusDto.CLOSED
                    ),
                    nextToken = null,
                    limit = null
                ),
                user = user
            )
            .getResult(ListChallengesResponseDto::class)
            .items
        assertEquals(registeredChallengesByStatus.values.flatten().size, challenges.size)
        assertTrue(challenges.isDistinct())

        val sortedChallenges = challenges.sortedWith { c1, c2 ->
            val priority = listOf(
                ChallengeStatusDto.IN_PROGRESS,
                ChallengeStatusDto.UPCOMING,
                ChallengeStatusDto.CLOSED,
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
        val registeredChallengesByStatus = transactionManager.doInTransaction { registerChallenges() }

        val inProgressChallenges = mvc
            .sccRequest(
                "/listChallenges",
                ListChallengesRequestDto(
                    statuses = listOf(ChallengeStatusDto.IN_PROGRESS),
                    nextToken = null,
                    limit = null
                )
            )
            .getResult(ListChallengesResponseDto::class)
            .items
        assertTrue(
            inProgressChallenges
                .map { it.status }
                .all { it == ChallengeStatusDto.IN_PROGRESS }
        )
        assertEquals(registeredChallengesByStatus[ChallengeStatus.IN_PROGRESS]?.size, inProgressChallenges.size)
        assertTrue(inProgressChallenges.isDistinct())

        val upcomingChallenges = mvc
            .sccRequest(
                "/listChallenges",
                ListChallengesRequestDto(
                    statuses = listOf(ChallengeStatusDto.UPCOMING),
                    nextToken = null,
                    limit = null
                )
            )
            .getResult(ListChallengesResponseDto::class)
            .items
        assertTrue(
            upcomingChallenges
                .map { it.status }
                .all { it == ChallengeStatusDto.UPCOMING }
        )
        assertEquals(registeredChallengesByStatus[ChallengeStatus.UPCOMING]?.size, upcomingChallenges.size)
        assertTrue(upcomingChallenges.isDistinct())

        val closedChallenges = mvc
            .sccRequest(
                "/listChallenges",
                ListChallengesRequestDto(
                    statuses = listOf(ChallengeStatusDto.CLOSED),
                    nextToken = null,
                    limit = null
                )
            )
            .getResult(ListChallengesResponseDto::class)
            .items
        assertTrue(
            closedChallenges
                .map { it.status }
                .all { it == ChallengeStatusDto.CLOSED }
        )
        assertEquals(registeredChallengesByStatus[ChallengeStatus.CLOSED]?.size, closedChallenges.size)
        assertTrue(closedChallenges.isDistinct())
    }

    @Test
    fun `필터가 여러 개 있는 경우 여러 필터에 해당하는 챌린지만 내려온다`() {
        val registeredChallengesByStatus = transactionManager.doInTransaction { registerChallenges() }

        val inProgressOrUpcomingChallenges = mvc
            .sccRequest(
                "/listChallenges",
                ListChallengesRequestDto(
                    statuses = listOf(ChallengeStatusDto.IN_PROGRESS, ChallengeStatusDto.UPCOMING),
                    nextToken = null,
                    limit = null
                )
            )
            .getResult(ListChallengesResponseDto::class)
            .items
        assertTrue(
            inProgressOrUpcomingChallenges
                .map { it.status }
                .all { it == ChallengeStatusDto.IN_PROGRESS || it == ChallengeStatusDto.UPCOMING }
        )
        assertEquals(
            (registeredChallengesByStatus[ChallengeStatus.IN_PROGRESS]?.size
                ?: 0) + (registeredChallengesByStatus[ChallengeStatus.UPCOMING]?.size ?: 0),
            inProgressOrUpcomingChallenges.size
        )
        assertTrue(inProgressOrUpcomingChallenges.isDistinct())

        val upcomingOrCloseChallenges = mvc
            .sccRequest(
                "/listChallenges",
                ListChallengesRequestDto(
                    statuses = listOf(ChallengeStatusDto.UPCOMING, ChallengeStatusDto.CLOSED),
                    nextToken = null,
                    limit = null
                )
            )
            .getResult(ListChallengesResponseDto::class)
            .items
        assertTrue(
            upcomingOrCloseChallenges
                .map { it.status }
                .all { it == ChallengeStatusDto.UPCOMING || it == ChallengeStatusDto.CLOSED }
        )
        assertEquals(
            (registeredChallengesByStatus[ChallengeStatus.UPCOMING]?.size
                ?: 0) + (registeredChallengesByStatus[ChallengeStatus.CLOSED]?.size ?: 0),
            upcomingOrCloseChallenges.size,
        )
        assertTrue(upcomingOrCloseChallenges.isDistinct())
    }

    @Test
    fun `진행 중 & 참여 중 챌린지는 앞쪽에 참여한 순서대로 차례대로 나온다`() {
        val registeredChallengesByStatus = transactionManager.doInTransaction { registerChallenges() }

        val inProgressChallengesBeforeParticipation = mvc
            .sccRequest(
                "/listChallenges",
                ListChallengesRequestDto(
                    statuses = listOf(ChallengeStatusDto.IN_PROGRESS),
                    nextToken = null,
                    limit = null
                )
            )
            .getResult(ListChallengesResponseDto::class)
            .items

        assertEquals(
            registeredChallengesByStatus[ChallengeStatus.IN_PROGRESS]?.size,
            inProgressChallengesBeforeParticipation.size
        )
        assertTrue(inProgressChallengesBeforeParticipation.isDistinct())

        val firstChallengeBeforeParticipation = inProgressChallengesBeforeParticipation.first()
        assertTrue(firstChallengeBeforeParticipation.status == ChallengeStatusDto.IN_PROGRESS)
        assertTrue(firstChallengeBeforeParticipation.hasJoined.not())
        val lastChallengeBeforeParticipation = inProgressChallengesBeforeParticipation.last()
        assertTrue(lastChallengeBeforeParticipation.status == ChallengeStatusDto.IN_PROGRESS)
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
                        ChallengeStatusDto.IN_PROGRESS,
                        ChallengeStatusDto.UPCOMING,
                        ChallengeStatusDto.CLOSED
                    ),
                    nextToken = null,
                    limit = null
                ),
                user = user
            )
            .getResult(ListChallengesResponseDto::class)
            .items

        assertEquals(registeredChallengesByStatus.values.flatten().size, challengesAfterParticipation.size)
        assertTrue(challengesAfterParticipation.isDistinct())

        val firstChallenge = challengesAfterParticipation.getOrNull(0)
        assertTrue(firstChallenge != null)
        assertEquals(firstChallengeBeforeParticipation.id, firstChallenge?.id)
        assertEquals(ChallengeStatusDto.IN_PROGRESS, firstChallenge?.status)
        assertEquals(true, firstChallenge?.hasJoined)
        val secondChallenge = challengesAfterParticipation.getOrNull(1)
        assertTrue(secondChallenge != null)
        assertEquals(lastChallengeBeforeParticipation.id, secondChallenge?.id)
        assertEquals(ChallengeStatusDto.IN_PROGRESS, secondChallenge?.status)
        assertEquals(true, secondChallenge?.hasJoined)
        val thirdChallenge = challengesAfterParticipation.getOrNull(2)
        assertTrue(thirdChallenge != null)
        assertEquals(ChallengeStatusDto.IN_PROGRESS, thirdChallenge?.status)
        assertEquals(false, thirdChallenge?.hasJoined)
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

    private fun List<ListChallengesItemDto>.isDistinct() = this.map { it.id }.let { it.size == it.toSet().size }
}
