package club.staircrusher.challenge.domain.model

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.place.PlaceCategory
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant

class ChallengeQuestTest {

    @Test
    fun `퀘스트가 완료되었는지 확인한다`() {
        // given
        val quest = createChallengeQuest(targetCount = 3)
        val progress = ChallengeQuestProgress.create("quest1")

        // when & then
        assertFalse(quest.isCompleted(progress))

        // when
        val contributedAt = SccClock.instant()
        progress.addContribution("contribution1", ChallengeActionCondition.Type.PLACE_ACCESSIBILITY, contributedAt, quest)
        progress.addContribution("contribution2", ChallengeActionCondition.Type.PLACE_ACCESSIBILITY, contributedAt, quest)
        // then
        assertFalse(quest.isCompleted(progress))

        // when
        progress.addContribution("contribution3", ChallengeActionCondition.Type.PLACE_ACCESSIBILITY, contributedAt, quest)
        // then
        assertTrue(quest.isCompleted(progress))
    }

    @Test
    fun `퀘스트 조건을 만족하는지 확인한다 - 액션 타입`() {
        // given
        val condition = ChallengeQuestCondition(
            targetCount = 3,
            startsAt = null,
            endsAt = null,
            actionConditions = listOf(ChallengeActionCondition.Type.PLACE_ACCESSIBILITY),
            placeCategories = null
        )
        val challengeStartsAt = Instant.parse("2024-01-01T00:00:00Z")
        val contributionCreatedAt = Instant.parse("2024-01-02T00:00:00Z")

        // 허용된 액션 타입
        assertTrue(condition.isSatisfied(
            ChallengeActionCondition.Type.PLACE_ACCESSIBILITY,
            null,
            challengeStartsAt,
            null,
            contributionCreatedAt
        ))

        // 허용되지 않은 액션 타입
        assertFalse(condition.isSatisfied(
            ChallengeActionCondition.Type.BUILDING_ACCESSIBILITY,
            null,
            challengeStartsAt,
            null,
            contributionCreatedAt
        ))
    }

    @Test
    fun `퀘스트 조건을 만족하는지 확인한다 - 장소 카테고리`() {
        // given
        val condition = ChallengeQuestCondition(
            targetCount = 3,
            startsAt = null,
            endsAt = null,
            actionConditions = listOf(ChallengeActionCondition.Type.PLACE_ACCESSIBILITY),
            placeCategories = listOf(PlaceCategory.CAFE, PlaceCategory.RESTAURANT)
        )
        val challengeStartsAt = Instant.parse("2024-01-01T00:00:00Z")
        val contributionCreatedAt = Instant.parse("2024-01-02T00:00:00Z")

        // 허용된 카테고리
        assertTrue(condition.isSatisfied(
            ChallengeActionCondition.Type.PLACE_ACCESSIBILITY,
            PlaceCategory.CAFE,
            challengeStartsAt,
            null,
            contributionCreatedAt
        ))

        // 허용되지 않은 카테고리
        assertFalse(condition.isSatisfied(
            ChallengeActionCondition.Type.PLACE_ACCESSIBILITY,
            PlaceCategory.HOSPITAL,
            challengeStartsAt,
            null,
            contributionCreatedAt
        ))

        // 카테고리가 null이면 허용하지 않음
        assertFalse(condition.isSatisfied(
            ChallengeActionCondition.Type.PLACE_ACCESSIBILITY,
            null,
            challengeStartsAt,
            null,
            contributionCreatedAt
        ))
    }

    @Test
    fun `퀘스트 조건을 만족하는지 확인한다 - 시간 범위`() {
        // given
        val questStartsAt = Instant.parse("2024-01-05T00:00:00Z")
        val questEndsAt = Instant.parse("2024-01-10T00:00:00Z")
        val condition = ChallengeQuestCondition(
            targetCount = 3,
            startsAt = questStartsAt,
            endsAt = questEndsAt,
            actionConditions = listOf(ChallengeActionCondition.Type.PLACE_ACCESSIBILITY),
            placeCategories = null
        )
        val challengeStartsAt = Instant.parse("2024-01-01T00:00:00Z")
        val challengeEndsAt = Instant.parse("2024-01-31T00:00:00Z")

        // 퀘스트 시작 전
        assertFalse(condition.isSatisfied(
            ChallengeActionCondition.Type.PLACE_ACCESSIBILITY,
            null,
            challengeStartsAt,
            challengeEndsAt,
            Instant.parse("2024-01-04T23:59:59Z")
        ))

        // 퀘스트 시작 시간
        assertTrue(condition.isSatisfied(
            ChallengeActionCondition.Type.PLACE_ACCESSIBILITY,
            null,
            challengeStartsAt,
            challengeEndsAt,
            questStartsAt
        ))

        // 퀘스트 기간 중
        assertTrue(condition.isSatisfied(
            ChallengeActionCondition.Type.PLACE_ACCESSIBILITY,
            null,
            challengeStartsAt,
            challengeEndsAt,
            Instant.parse("2024-01-07T12:00:00Z")
        ))

        // 퀘스트 종료 후
        assertFalse(condition.isSatisfied(
            ChallengeActionCondition.Type.PLACE_ACCESSIBILITY,
            null,
            challengeStartsAt,
            challengeEndsAt,
            Instant.parse("2024-01-10T00:00:01Z")
        ))
    }

    private fun createChallengeQuest(targetCount: Int = 3): ChallengeQuest {
        return ChallengeQuest(
            id = "quest1",
            title = "테스트 퀘스트",
            description = "테스트용 퀘스트입니다",
            condition = ChallengeQuestCondition(
                targetCount = targetCount,
                startsAt = null,
                endsAt = null,
                actionConditions = listOf(ChallengeActionCondition.Type.PLACE_ACCESSIBILITY),
                placeCategories = null
            )
        )
    }
}
