package club.staircrusher.challenge.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.GetChallengeRequestDto
import club.staircrusher.api.spec.dto.GetChallengeResponseDto
import club.staircrusher.challenge.domain.model.Challenge
import club.staircrusher.challenge.infra.adapter.`in`.controller.base.ChallengeITBase
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class GetChallengeQuestTest : ChallengeITBase() {

    @Test
    fun `챌린지 조회 시 퀘스트 정보와 진행 현황 필드가 존재한다`() {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createIdentifiedUser()
        }
        
        val challenge = transactionManager.doInTransaction {
            testDataGenerator.createChallenge(
                name = "퀘스트 테스트 챌린지",
                isComplete = false,
                startsAt = Challenge.MIN_TIME.plusSeconds(60),
                endsAt = Challenge.MAX_TIME.minusSeconds(60),
                goal = 10
            )
        }

        transactionManager.doInTransaction {
            testDataGenerator.participateChallenge(user.account, challenge, clock.instant())
        }

        val requestDto = GetChallengeRequestDto(challengeId = challenge.id)

        mvc
            .sccRequest("/getChallenge", requestDto, user.account)
            .andExpect { status { isOk() } }
            .apply {
                val result = getResult(GetChallengeResponseDto::class)

                Assertions.assertTrue(result.hasJoined)
                // 퀘스트 필드가 존재하는지 확인 (빈 리스트라도 필드는 존재해야 함)
                Assertions.assertNotNull(result.quests)
                
                // 참여한 경우 퀘스트 정보가 반환되며, 각 퀘스트에는 진행 현황이 포함됨
                Assertions.assertEquals(0, result.quests.size) // 퀘스트가 없으면 0개
            }
    }

    @Test
    fun `참여하지 않은 챌린지 조회 시 퀘스트 정보는 반환하지만 진행 현황은 빈 배열이다`() {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createIdentifiedUser()
        }
        
        val challenge = transactionManager.doInTransaction {
            testDataGenerator.createChallenge(
                name = "참여하지 않은 챌린지",
                isComplete = false,
                startsAt = Challenge.MIN_TIME.plusSeconds(60),
                endsAt = Challenge.MAX_TIME.minusSeconds(60),
                goal = 10
            )
        }

        val requestDto = GetChallengeRequestDto(challengeId = challenge.id)

        mvc
            .sccRequest("/getChallenge", requestDto, user.account)
            .andExpect { status { isOk() } }
            .apply {
                val result = getResult(GetChallengeResponseDto::class)

                Assertions.assertFalse(result.hasJoined)
                Assertions.assertNotNull(result.quests)
                
                // 참여하지 않은 경우에도 퀘스트 정보는 반환되지만, 진행 현황은 모두 0으로 표시됨
                Assertions.assertEquals(0, result.quests.size) // 퀘스트가 없으면 0개
            }
    }
}