package club.staircrusher.challenge.domain.model

import club.staircrusher.stdlib.clock.SccClock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class ChallengeUpdateTest {

    @Test
    fun `챌린지를 수정할 수 있다`() {
        // given
        val startTime = SccClock.instant().minusSeconds(3600) // 1시간 전
        val challenge = createChallenge(startsAt = startTime)

        val updateRequest = UpdateChallengeRequest(
            id = challenge.id,
            name = "Updated Challenge",
            endsAt = null,
            description = "Updated description",
            crusherGroup = null,
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

}
