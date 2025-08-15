package club.staircrusher.challenge.domain.model

import club.staircrusher.stdlib.clock.SccClock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ChallengeQuestProgressTest {

    @Test
    fun `기여를 추가하면 카운트가 증가한다`() {
        // given
        val progress = ChallengeQuestProgress.create("quest1")

        // when
        val quest = createChallengeQuest(targetCount = 3)
        val contributedAt = SccClock.instant()
        progress.addContribution("contribution1", ChallengeActionCondition.Type.PLACE_ACCESSIBILITY, contributedAt, quest)

        // then
        assertEquals(1, progress.completedCount)
        assertEquals(listOf("contribution1"), progress.contributionIds)
        assertEquals(1, progress.contributions.size)
        assertEquals("contribution1", progress.contributions[0].contributionId)
        assertEquals(ChallengeActionCondition.Type.PLACE_ACCESSIBILITY, progress.contributions[0].actionType)
        assertEquals(contributedAt, progress.contributions[0].contributedAt)
    }

    @Test
    fun `동일한 기여를 여러 번 추가해도 카운트는 한 번만 증가한다`() {
        // given
        val progress = ChallengeQuestProgress.create("quest1")

        // when
        val quest = createChallengeQuest(targetCount = 3)
        val contributedAt = SccClock.instant()
        progress.addContribution("contribution1", ChallengeActionCondition.Type.PLACE_ACCESSIBILITY, contributedAt, quest)
        progress.addContribution("contribution1", ChallengeActionCondition.Type.BUILDING_ACCESSIBILITY, contributedAt, quest)

        // then
        assertEquals(1, progress.completedCount)
        assertEquals(listOf("contribution1"), progress.contributionIds)
    }

    @Test
    fun `기여를 제거하면 카운트가 감소한다`() {
        // given
        val quest = createChallengeQuest(targetCount = 3)
        val progress = ChallengeQuestProgress.create("quest1")
        val contributedAt = SccClock.instant()
        progress.addContribution("contribution1", ChallengeActionCondition.Type.PLACE_ACCESSIBILITY, contributedAt, quest)
        progress.addContribution("contribution2", ChallengeActionCondition.Type.PLACE_ACCESSIBILITY, contributedAt, quest)

        // when
        progress.removeContribution("contribution1", quest)

        // then
        assertEquals(1, progress.completedCount)
        assertEquals(listOf("contribution2"), progress.contributionIds)
    }

    @Test
    fun `퀘스트가 더 이상 완료되지 않으면 완료 시간이 리셋된다`() {
        // given
        val quest = createChallengeQuest(targetCount = 2)
        val progress = ChallengeQuestProgress.create("quest1")
        val contributedAt = SccClock.instant()
        progress.addContribution("contribution1", ChallengeActionCondition.Type.PLACE_ACCESSIBILITY, contributedAt, quest)
        progress.addContribution("contribution2", ChallengeActionCondition.Type.PLACE_ACCESSIBILITY, contributedAt, quest)

        assertTrue(progress.isCompleted)

        // when
        progress.removeContribution("contribution1", quest)

        // then
        assertEquals(1, progress.completedCount)
        assertNull(progress.completedAt)
        assertFalse(progress.isCompleted)
    }

    @Test
    fun `완료된 상태에서는 isCompleted가 true이다`() {
        // given
        val progress = ChallengeQuestProgress.create("quest1")

        // when - 완료 상태로 만들기 위해 한 개 기여 추가
        val quest = createChallengeQuest(targetCount = 1)
        val contributedAt = SccClock.instant()
        progress.addContribution("contribution1", ChallengeActionCondition.Type.PLACE_ACCESSIBILITY, contributedAt, quest)

        // then
        assertTrue(progress.isCompleted)
        assertNotNull(progress.completedAt)
    }

    @Test
    fun `이미 완료된 상태에서 다시 완료해도 완료 시간은 변경되지 않는다`() {
        // given
        val progress = ChallengeQuestProgress.create("quest1")
        val quest = createChallengeQuest(targetCount = 1)
        val contributedAt = SccClock.instant()
        progress.addContribution("contribution1", ChallengeActionCondition.Type.PLACE_ACCESSIBILITY, contributedAt, quest)
        val firstCompletedAt = progress.completedAt

        // when - 이미 완료된 상태에서 다시 기여 추가해도 완료 시간은 변경되지 않음
        Thread.sleep(1) // 시간 차이를 만들기 위해
        progress.addContribution("contribution2", ChallengeActionCondition.Type.PLACE_ACCESSIBILITY, contributedAt, quest)

        // then
        assertEquals(firstCompletedAt, progress.completedAt)
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
