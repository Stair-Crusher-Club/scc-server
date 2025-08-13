package club.staircrusher.challenge.domain.model

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.domain.SccDomainException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant

class ChallengeUpdateTest {

    @Test
    fun `시작 예정인 챌린지는 퀘스트를 수정할 수 있다`() {
        // given - 미래에 시작하는 챌린지
        val futureStartTime = SccClock.instant().plusSeconds(3600) // 1시간 후
        val challenge = createChallenge(startsAt = futureStartTime)

        val newQuests = listOf(
            createChallengeQuest("quest1", "새로운 퀘스트")
        )

        val updateRequest = UpdateChallengeRequest(
            id = challenge.id,
            name = "Updated Challenge",
            endsAt = null,
            description = "Updated description",
            crusherGroup = null,
            quests = newQuests
        )

        // when
        challenge.update(updateRequest)

        // then
        assertEquals(newQuests, challenge.quests)
    }

    @Test
    fun `진행 중인 챌린지는 퀘스트를 수정할 수 없다`() {
        // given - 현재 진행 중인 챌린지
        val pastStartTime = SccClock.instant().minusSeconds(3600) // 1시간 전
        val challenge = createChallenge(startsAt = pastStartTime)

        val newQuests = listOf(
            createChallengeQuest("quest1", "새로운 퀘스트")
        )

        val updateRequest = UpdateChallengeRequest(
            id = challenge.id,
            name = "Updated Challenge",
            endsAt = null,
            description = "Updated description",
            crusherGroup = null,
            quests = newQuests
        )

        // when & then
        val exception = assertThrows(SccDomainException::class.java) {
            challenge.update(updateRequest)
        }
        assertEquals("시작된 챌린지의 퀘스트는 수정할 수 없습니다.", exception.message)
    }

    @Test
    fun `퀘스트 수정 없이는 언제든지 챌린지를 수정할 수 있다`() {
        // given - 진행 중인 챌린지
        val pastStartTime = SccClock.instant().minusSeconds(3600) // 1시간 전
        val challenge = createChallenge(startsAt = pastStartTime)

        val updateRequest = UpdateChallengeRequest(
            id = challenge.id,
            name = "Updated Challenge",
            endsAt = null,
            description = "Updated description",
            crusherGroup = null,
            quests = null // 퀘스트 수정 없음
        )

        // when
        challenge.update(updateRequest)

        // then
        assertEquals("Updated Challenge", challenge.name)
        assertEquals("Updated description", challenge.description)
    }

    private fun createChallenge(startsAt: Instant): Challenge {
        return Challenge(
            id = "challenge1",
            name = "Test Challenge",
            isPublic = true,
            invitationCode = null,
            passcode = null,
            isB2B = false,
            crusherGroup = null,
            isComplete = false,
            startsAt = startsAt,
            endsAt = null,
            goal = 10,
            milestones = emptyList(),
            conditions = emptyList(),
            quests = emptyList(),
            createdAt = SccClock.instant(),
            updatedAt = SccClock.instant(),
            description = "Test description"
        )
    }

    private fun createChallengeQuest(id: String, title: String): ChallengeQuest {
        return ChallengeQuest(
            id = id,
            title = title,
            description = "테스트용 퀘스트입니다",
            condition = ChallengeQuestCondition(
                targetCount = 3,
                startsAt = null,
                endsAt = null,
                actionConditions = listOf(ChallengeActionCondition.Type.PLACE_ACCESSIBILITY),
                placeCategories = null
            )
        )
    }
}
