package club.staircrusher.challenge.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.ApiErrorResponse
import club.staircrusher.api.spec.dto.JoinChallengeRequestDto
import club.staircrusher.api.spec.dto.JoinChallengeResponseDto
import club.staircrusher.challenge.application.port.out.persistence.ChallengeContributionRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeParticipationRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeRepository
import club.staircrusher.challenge.infra.adapter.`in`.controller.base.ChallengeITBase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class JoinChallengeTest : ChallengeITBase() {
    @Autowired
    private lateinit var challengeRepository: ChallengeRepository

    @Autowired
    private lateinit var challengeContributionRepository: ChallengeContributionRepository

    @Autowired
    private lateinit var challengeParticipationRepository: ChallengeParticipationRepository

    @BeforeEach
    fun setUp() = transactionManager.doInTransaction {
        challengeRepository.removeAll()
    }

    @Test
    fun `참여하고 있지 않은 챌린지에 참여 요청 시 참여 완료`() {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createUser()
        }
        val inProgressChallenge = registerInProgressChallenge()
        val response = mvc
            .sccRequest(
                "/joinChallenge",
                JoinChallengeRequestDto(
                    challengeId = inProgressChallenge.id,
                    passcode = null
                ),
                user = user
            )
            .getResult(JoinChallengeResponseDto::class)
        assert(response.challenge.id == inProgressChallenge.id)
    }

    @Test
    fun `이미 참여한 챌린지는 이미 참여했음을 알리는 에러가 난다`() {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createUser()
        }
        val inProgressChallenge = registerInProgressChallenge()
        mvc
            .sccRequest(
                "/joinChallenge",
                JoinChallengeRequestDto(
                    challengeId = inProgressChallenge.id,
                    passcode = null
                ),
                user = user
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
                user = user
            )
            .getResult(ApiErrorResponse::class)
            .apply {
                assert(this.code == ApiErrorResponse.Code.ALREADY_JOINED)
            }
    }

    @Test
    fun `참여 코드가 필요한 챌린지에 참여 코드를 알맞게 입력하면 참여 완료`() {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createUser()
        }
        val inProgressChallenge = registerInProgressChallenge(passcode = "test")
        val response = mvc
            .sccRequest(
                "/joinChallenge",
                JoinChallengeRequestDto(
                    challengeId = inProgressChallenge.id,
                    passcode = "test"
                ),
                user = user
            )
            .getResult(JoinChallengeResponseDto::class)
        assert(response.challenge.id == inProgressChallenge.id)
    }

    @Test
    fun `참여 코드가 필요한 챌린지에 참여코드가 없거나 다르면 에러가 난다`() {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createUser()
        }
        val inProgressChallenge = registerInProgressChallenge(passcode = "test")
        mvc
            .sccRequest(
                "/joinChallenge",
                JoinChallengeRequestDto(
                    challengeId = inProgressChallenge.id,
                    passcode = null
                ),
                user = user
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
                user = user
            )
            .getResult(ApiErrorResponse::class)
            .apply {
                assert(this.code == ApiErrorResponse.Code.INVALID_PASSCODE)
            }
    }

    @Test
    fun `종료되거나 오픈예정인 챌린지에는 참여할 수 없다`() {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createUser()
        }
        val upcomingChallenge = registerUpcomingChallenge()
        mvc
            .sccRequest(
                "/joinChallenge",
                JoinChallengeRequestDto(
                    challengeId = upcomingChallenge.id,
                    passcode = null
                ),
                user = user
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
                user = user
            )
            .getResult(ApiErrorResponse::class)
            .apply {
                assert(this.code == ApiErrorResponse.Code.CHALLENGE_CLOSED)
            }
    }
}
