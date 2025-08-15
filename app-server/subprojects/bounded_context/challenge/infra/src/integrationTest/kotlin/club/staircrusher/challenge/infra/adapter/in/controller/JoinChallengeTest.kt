package club.staircrusher.challenge.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.ApiErrorResponse
import club.staircrusher.api.spec.dto.JoinChallengeRequestCompanyJoinInfoDto
import club.staircrusher.api.spec.dto.JoinChallengeRequestDto
import club.staircrusher.api.spec.dto.JoinChallengeResponseDto
import club.staircrusher.challenge.application.port.out.persistence.ChallengeParticipationRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeRepository
import club.staircrusher.challenge.infra.adapter.`in`.controller.base.ChallengeITBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
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

    @Test
    fun `B2B 챌린지에 회사명과 개인명을 입력하면 참여 완료`() {
        val userAccount = transactionManager.doInTransaction {
            testDataGenerator.createIdentifiedUser().account
        }
        val inProgressChallenge = registerInProgressChallenge()
        val response = mvc
            .sccRequest(
                "/joinChallenge",
                JoinChallengeRequestDto(
                    challengeId = inProgressChallenge.id,
                    passcode = null,
                    companyInfo = JoinChallengeRequestCompanyJoinInfoDto(
                        companyName = "StairCrusher",
                        participantName = "홍길동"
                    )
                ),
                userAccount = userAccount
            )
            .getResult(JoinChallengeResponseDto::class)
        assert(response.challenge.id == inProgressChallenge.id)

        // Check that company name and personal name are recorded in participation
        val participation = challengeParticipationRepository.findByChallengeIdAndUserId(inProgressChallenge.id, userAccount.id)[0]
        assertEquals("홍길동", participation.participantName)
        assertEquals("StairCrusher", participation.companyName)
    }

    @Test
    fun `일반 챌린지에는 회사 정보 없이 참여할 수 있다`() {
        val userAccount = transactionManager.doInTransaction {
            testDataGenerator.createIdentifiedUser().account
        }
        val inProgressChallenge = registerInProgressChallenge()

        // Test with no company info - should succeed for non-B2B challenge
        val response = mvc
            .sccRequest(
                "/joinChallenge",
                JoinChallengeRequestDto(
                    challengeId = inProgressChallenge.id,
                    passcode = null,
                    companyInfo = null
                ),
                userAccount = userAccount
            )
            .getResult(JoinChallengeResponseDto::class)
        assert(response.challenge.id == inProgressChallenge.id)

        // Check that participation has no company info
        val participation = challengeParticipationRepository.findByChallengeIdAndUserId(inProgressChallenge.id, userAccount.id)[0]
        assertNull(participation.participantName)
        assertNull(participation.companyName)
    }

    @Test
    fun `B2B 챌린지에는 회사명과 참여자명이 필수이다`() {
        val userAccount = transactionManager.doInTransaction {
            testDataGenerator.createIdentifiedUser().account
        }
        
        // Create B2B challenge
        val b2bChallenge = transactionManager.doInTransaction {
            testDataGenerator.createChallenge(
                name = "B2B 챌린지",
                isB2B = true,
                startsAt = clock.instant().minusSeconds(3600),
                endsAt = clock.instant().plusSeconds(3600),
                conditions = emptyList()
            )
        }

        // Test without company info - should fail
        mvc
            .sccRequest(
                "/joinChallenge",
                JoinChallengeRequestDto(
                    challengeId = b2bChallenge.id,
                    passcode = null,
                    companyInfo = null
                ),
                userAccount = userAccount
            )
            .getResult(ApiErrorResponse::class)
            .apply {
                assert(this.code == ApiErrorResponse.Code.B2B_INFO_REQUIRED)
            }

        // Test with incomplete company info - should fail
        mvc
            .sccRequest(
                "/joinChallenge",
                JoinChallengeRequestDto(
                    challengeId = b2bChallenge.id,
                    passcode = null,
                    companyInfo = JoinChallengeRequestCompanyJoinInfoDto(
                        companyName = "StairCrusher",
                        participantName = ""  // Empty participant name
                    )
                ),
                userAccount = userAccount
            )
            .getResult(ApiErrorResponse::class)
            .apply {
                assert(this.code == ApiErrorResponse.Code.B2B_INFO_REQUIRED)
            }

        // Test with complete company info - should succeed
        val response = mvc
            .sccRequest(
                "/joinChallenge",
                JoinChallengeRequestDto(
                    challengeId = b2bChallenge.id,
                    passcode = null,
                    companyInfo = JoinChallengeRequestCompanyJoinInfoDto(
                        companyName = "StairCrusher",
                        participantName = "홍길동"
                    )
                ),
                userAccount = userAccount
            )
            .getResult(JoinChallengeResponseDto::class)
        assert(response.challenge.id == b2bChallenge.id)

        // Check that participation has company info
        val participation = challengeParticipationRepository.findByChallengeIdAndUserId(b2bChallenge.id, userAccount.id)[0]
        assertEquals("홍길동", participation.participantName)
        assertEquals("StairCrusher", participation.companyName)
    }
}
