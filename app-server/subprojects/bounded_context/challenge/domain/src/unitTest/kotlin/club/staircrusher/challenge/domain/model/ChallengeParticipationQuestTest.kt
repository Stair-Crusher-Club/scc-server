package club.staircrusher.challenge.domain.model

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.place.PlaceCategory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant

class ChallengeParticipationQuestTest {

    @Test
    fun `퀘스트 진행도는 필요할 때 자동으로 생성된다`() {
        // given
        val participation = createChallengeParticipation()
        val quest = createChallengeQuest("quest1", "첫 번째 퀘스트")

        val challengeStartsAt = Instant.parse("2024-01-01T00:00:00Z")
        val contributionCreatedAt = Instant.parse("2024-01-02T00:00:00Z")

        // when - 첫 번째 기여 시 자동으로 퀘스트 진행도가 생성됨
        participation.updateQuestProgress(
            challengeQuests = listOf(quest),
            contributionId = "contribution1",
            actionType = ChallengeActionCondition.Type.PLACE_ACCESSIBILITY,
            placeCategory = null,
            challengeStartsAt = challengeStartsAt,
            challengeEndsAt = null,
            contributionCreatedAt = contributionCreatedAt
        )

        // then
        assertEquals(1, participation.questProgresses!!.size)
        assertEquals("quest1", participation.questProgresses!![0].questId)
        assertEquals(1, participation.questProgresses!![0].completedCount)
    }

    @Test
    fun `조건에 맞는 기여가 추가되면 퀘스트 진행도가 업데이트된다`() {
        // given
        val participation = createChallengeParticipation()
        val quest = createChallengeQuest("quest1", "테스트 퀘스트", targetCount = 2)

        val challengeStartsAt = Instant.parse("2024-01-01T00:00:00Z")
        val contributionCreatedAt = Instant.parse("2024-01-02T00:00:00Z")

        // when
        participation.updateQuestProgress(
            challengeQuests = listOf(quest),
            contributionId = "contribution1",
            actionType = ChallengeActionCondition.Type.PLACE_ACCESSIBILITY,
            placeCategory = null,
            challengeStartsAt = challengeStartsAt,
            challengeEndsAt = null,
            contributionCreatedAt = contributionCreatedAt
        )

        // then
        val progress = participation.questProgresses!!.first { it.questId == "quest1" }
        assertEquals(1, progress.completedCount)
        assertEquals(listOf("contribution1"), progress.contributionIds)
        assertFalse(progress.isCompleted)
    }

    @Test
    fun `목표 달성 시 퀘스트가 완료된다`() {
        // given
        val participation = createChallengeParticipation()
        val quest = createChallengeQuest("quest1", "테스트 퀘스트", targetCount = 1)

        val challengeStartsAt = Instant.parse("2024-01-01T00:00:00Z")
        val contributionCreatedAt = Instant.parse("2024-01-02T00:00:00Z")

        // when
        participation.updateQuestProgress(
            challengeQuests = listOf(quest),
            contributionId = "contribution1",
            actionType = ChallengeActionCondition.Type.PLACE_ACCESSIBILITY,
            placeCategory = null,
            challengeStartsAt = challengeStartsAt,
            challengeEndsAt = null,
            contributionCreatedAt = contributionCreatedAt
        )

        // then
        val progress = participation.questProgresses!!.first { it.questId == "quest1" }
        assertEquals(1, progress.completedCount)
        assertTrue(progress.isCompleted)
        assertNotNull(progress.completedAt)
    }

    @Test
    fun `조건에 맞지 않는 기여는 퀘스트 진행도에 반영되지 않는다`() {
        // given
        val participation = createChallengeParticipation()
        val quest = createChallengeQuest(
            "quest1",
            "카페 전용 퀘스트",
            placeCategories = listOf(PlaceCategory.CAFE)
        )

        val challengeStartsAt = Instant.parse("2024-01-01T00:00:00Z")
        val contributionCreatedAt = Instant.parse("2024-01-02T00:00:00Z")

        // when - 음식점 카테고리로 기여 (카페가 아님)
        participation.updateQuestProgress(
            challengeQuests = listOf(quest),
            contributionId = "contribution1",
            actionType = ChallengeActionCondition.Type.PLACE_ACCESSIBILITY,
            placeCategory = PlaceCategory.RESTAURANT,
            challengeStartsAt = challengeStartsAt,
            challengeEndsAt = null,
            contributionCreatedAt = contributionCreatedAt
        )

        // then - 조건에 맞지 않아 진행도가 생성되지 않음
        assertTrue(participation.questProgresses!!.isEmpty())
    }

    @Test
    fun `기여를 제거하면 퀘스트 진행도가 감소한다`() {
        // given
        val participation = createChallengeParticipation()
        val quest = createChallengeQuest("quest1", "테스트 퀘스트", targetCount = 2)

        // 기여 추가
        val challengeStartsAt = Instant.parse("2024-01-01T00:00:00Z")
        val contributionCreatedAt = Instant.parse("2024-01-02T00:00:00Z")

        participation.updateQuestProgress(
            challengeQuests = listOf(quest),
            contributionId = "contribution1",
            actionType = ChallengeActionCondition.Type.PLACE_ACCESSIBILITY,
            placeCategory = null,
            challengeStartsAt = challengeStartsAt,
            challengeEndsAt = null,
            contributionCreatedAt = contributionCreatedAt
        )

        participation.updateQuestProgress(
            challengeQuests = listOf(quest),
            contributionId = "contribution2",
            actionType = ChallengeActionCondition.Type.PLACE_ACCESSIBILITY,
            placeCategory = null,
            challengeStartsAt = challengeStartsAt,
            challengeEndsAt = null,
            contributionCreatedAt = contributionCreatedAt
        )

        // when - 기여 제거
        participation.removeQuestProgress("contribution1", listOf(quest))

        // then
        val progress = participation.questProgresses!!.first { it.questId == "quest1" }
        assertEquals(1, progress.completedCount)
        assertEquals(listOf("contribution2"), progress.contributionIds)
    }

    private fun createChallengeParticipation(): ChallengeParticipation {
        return ChallengeParticipation(
            id = "participation1",
            challengeId = "challenge1",
            userId = "user1",
            participantName = null,
            companyName = null,
            questProgresses = emptyList(),
            createdAt = SccClock.instant()
        )
    }

    private fun createChallengeQuest(
        id: String,
        title: String,
        targetCount: Int = 3,
        placeCategories: List<PlaceCategory>? = null
    ): ChallengeQuest {
        return ChallengeQuest(
            id = id,
            title = title,
            description = "테스트용 퀘스트입니다",
            condition = ChallengeQuestCondition(
                targetCount = targetCount,
                startsAt = null,
                endsAt = null,
                actionConditions = listOf(ChallengeActionCondition.Type.PLACE_ACCESSIBILITY),
                placeCategories = placeCategories
            )
        )
    }
}
