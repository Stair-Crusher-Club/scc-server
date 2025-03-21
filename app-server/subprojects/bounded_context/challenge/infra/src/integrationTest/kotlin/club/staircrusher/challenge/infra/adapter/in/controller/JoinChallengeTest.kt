package club.staircrusher.challenge.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.ApiErrorResponse
import club.staircrusher.api.spec.dto.JoinChallengeRequestDto
import club.staircrusher.api.spec.dto.JoinChallengeResponseDto
import club.staircrusher.challenge.application.port.out.persistence.ChallengeParticipationRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeRepository
import club.staircrusher.challenge.infra.adapter.`in`.controller.base.ChallengeITBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class JoinChallengeTest : ChallengeITBase() {
    @Autowired
    private lateinit var challengeRepository: ChallengeRepository

    @Autowired
    private lateinit var challengeParticipationRepository: ChallengeParticipationRepository

    @BeforeEach
    fun setUp() = transactionManager.doInTransaction {
        challengeRepository.deleteAll()
    }

    @Test
    fun `참여하고 있지 않은 챌린지에 참여 요청 시 참여 완료`() {
        val userAccount = transactionManager.doInTransaction {
            testDataGenerator.createIdentifiedUser().account
        }
        val inProgressChallenge = registerInProgressChallenge()
        val response = mvc
            .sccRequest(
                "/joinChallenge",
                JoinChallengeRequestDto(
                    challengeId = inProgressChallenge.id,
                    passcode = null
                ),
                userAccount = userAccount
            )
            .getResult(JoinChallengeResponseDto::class)
        assert(response.challenge.id == inProgressChallenge.id)
    }

    @Test
    fun `이미 참여한 챌린지에 참여하려고 하면 멱등적으로 처리된다`() {
        val userAccount = transactionManager.doInTransaction {
            testDataGenerator.createIdentifiedUser().account
        }
        val inProgressChallenge = registerInProgressChallenge()
        mvc
            .sccRequest(
                "/joinChallenge",
                JoinChallengeRequestDto(
                    challengeId = inProgressChallenge.id,
                    passcode = null
                ),
                userAccount = userAccount
            )
            .getResult(JoinChallengeResponseDto::class)
            .apply {
                assert(challenge.id == inProgressChallenge.id)
            }
        mvc
            .sccRequest(
                "/joinChallenge",
                JoinChallengeRequestDto(
                    challengeId = inProgressChallenge.id,
                    passcode = null
                ),
                userAccount = userAccount
            )
            .getResult(JoinChallengeResponseDto::class)
            .apply {
                assert(challenge.id == inProgressChallenge.id)
                assertEquals(1, challengeParticipationRepository.findByChallengeIdAndUserId(challenge.id, userAccount.id).size)
            }
    }

    @Test
    fun `참여 코드가 필요한 챌린지에 참여 코드를 알맞게 입력하면 참여 완료`() {
        val userAccount = transactionManager.doInTransaction {
            testDataGenerator.createIdentifiedUser().account
        }
        val inProgressChallenge = registerInProgressChallenge(passcode = "test")
        val response = mvc
            .sccRequest(
                "/joinChallenge",
                JoinChallengeRequestDto(
                    challengeId = inProgressChallenge.id,
                    passcode = "test"
                ),
                userAccount = userAccount
            )
            .getResult(JoinChallengeResponseDto::class)
        assert(response.challenge.id == inProgressChallenge.id)
    }

    @Test
    fun `참여 코드가 필요한 챌린지에 참여코드가 없거나 다르면 에러가 난다`() {
        val userAccount = transactionManager.doInTransaction {
            testDataGenerator.createIdentifiedUser().account
        }
        val inProgressChallenge = registerInProgressChallenge(passcode = "test")
        mvc
            .sccRequest(
                "/joinChallenge",
                JoinChallengeRequestDto(
                    challengeId = inProgressChallenge.id,
                    passcode = null
                ),
                userAccount = userAccount
            )
            .getResult(ApiErrorResponse::class)
            .apply {
                assert(this.code == ApiErrorResponse.Code.INVALID_PASSCODE)
            }
        mvc
            .sccRequest(
                "/joinChallenge",
                JoinChallengeRequestDto(
                    challengeId = inProgressChallenge.id,
                    passcode = "wrong_passcode"
                ),
                userAccount = userAccount
            )
            .getResult(ApiErrorResponse::class)
            .apply {
                assert(this.code == ApiErrorResponse.Code.INVALID_PASSCODE)
            }
    }

    @Test
    fun `종료되거나 오픈예정인 챌린지에는 참여할 수 없다`() {
        val userAccount = transactionManager.doInTransaction {
            testDataGenerator.createIdentifiedUser().account
        }
        val upcomingChallenge = registerUpcomingChallenge()
        mvc
            .sccRequest(
                "/joinChallenge",
                JoinChallengeRequestDto(
                    challengeId = upcomingChallenge.id,
                    passcode = null
                ),
                userAccount = userAccount
            )
            .getResult(ApiErrorResponse::class)
            .apply {
                assert(this.code == ApiErrorResponse.Code.CHALLENGE_NOT_OPENED)
            }
        val closedChallenge = registerClosedChallenge()
        mvc
            .sccRequest(
                "/joinChallenge",
                JoinChallengeRequestDto(
                    challengeId = closedChallenge.id,
                    passcode = null
                ),
                userAccount = userAccount
            )
            .getResult(ApiErrorResponse::class)
            .apply {
                assert(this.code == ApiErrorResponse.Code.CHALLENGE_CLOSED)
            }
    }
}
