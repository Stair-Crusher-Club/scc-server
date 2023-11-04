package club.staircrusher.challenge.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.GetChallengeRequestDto
import club.staircrusher.api.spec.dto.GetChallengeResponseDto
import club.staircrusher.api.spec.dto.GetChallengeWithInvitationCodeRequestDto
import club.staircrusher.challenge.application.port.out.persistence.ChallengeContributionRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeParticipationRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeRepository
import club.staircrusher.challenge.infra.adapter.`in`.controller.base.ChallengeITBase
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.random.Random

class GetChallengeTest : ChallengeITBase() {
    @Autowired
    private lateinit var challengeRepository: ChallengeRepository

    @Autowired
    private lateinit var challengeContributionRepository: ChallengeContributionRepository

    @Autowired
    private lateinit var challengeParticipationRepository: ChallengeParticipationRepository

    @BeforeEach
    fun setUp() {
        transactionManager.doInTransaction {
            challengeRepository.removeAll()
            challengeParticipationRepository.removeAll()
            challengeContributionRepository.removeAll()

            registerChallenges()
        }
    }

    @Test
    fun `참여자 수, 정복한 장소 수가 맞는지 확인한다`() {
        val challenge = registerInProgressChallenge()
        // 참여 전 상태 확인
        val challengeBeforeParticipationAndContribution = mvc.sccRequest(
            "/getChallenge",
            GetChallengeRequestDto(challengeId = challenge.id),
        )
            .getResult(GetChallengeResponseDto::class)
            .challenge
        assertTrue(challengeBeforeParticipationAndContribution.participationsCount == 0)
        assertTrue(challengeBeforeParticipationAndContribution.contributionsCount == 0)

        val users = transactionManager.doInTransaction {
            (0 until Random.nextLong(from = 1, until = 10))
                .map {
                    val user = testDataGenerator.createUser()
                    participate(user, challenge)
                    return@map user
                }
        }
        val contributions = (0 until Random.nextLong(from = 10, until = 100))
            .map {
                contributePlaceAccessibility(
                    user = users.random(),
                    challenge = challenge
                )
            }

        mvc.sccRequest(
            "/updateChallengeRanks",
            null,
        )
            .andReturn()

        val getChallengeResponse = mvc.sccRequest(
            "/getChallenge",
            GetChallengeRequestDto(
                challengeId = challenge.id
            ),
            user = users.first()
        )
            .getResult(GetChallengeResponseDto::class)
        // 참여 후 상태 확인
        assertTrue(getChallengeResponse.challenge.id == challenge.id)
        assertTrue(getChallengeResponse.challenge.participationsCount == users.count())
        assertTrue(getChallengeResponse.challenge.contributionsCount == contributions.count())
        assertTrue(getChallengeResponse.hasJoined)
        assertTrue(getChallengeResponse.myRank != null)
    }

    @Test
    fun `참여코드가 있는 챌린지는 참여코드로도 조회가 가능하다`() {
        val user = transactionManager.doInTransaction { testDataGenerator.createUser() }
        val invitationCode = "VCNC 모여라"
        val inProgressChallenge = registerInProgressChallenge(invitationCode = invitationCode)
        val challengeByInvitationCode = mvc.sccRequest(
            "/getChallengeWithInvitationCode",
            GetChallengeWithInvitationCodeRequestDto(invitationCode = invitationCode),
            user = user
        )
            .getResult(GetChallengeResponseDto::class)
            .challenge
        assertTrue(inProgressChallenge.id == challengeByInvitationCode.id)
    }

    @Test
    fun `정복한 장소가 목표치에 도달하면 완료 상태를 만든다`() {
        val goal = 10
        val challenge = registerInProgressChallenge(goal = goal)
        val users = transactionManager.doInTransaction {
            (0 until Random.nextLong(from = 1, until = 10))
                .map {
                    val user = testDataGenerator.createUser()
                    participate(user, challenge)
                    return@map user
                }
        }
        (0 until goal - 1)
            .map {
                contributePlaceAccessibility(
                    user = users.random(),
                    challenge = challenge
                )
            }
        val challengeBeforeComplete = mvc.sccRequest(
            "/getChallenge",
            GetChallengeRequestDto(
                challengeId = challenge.id
            ),
            user = users.first()
        )
            .getResult(GetChallengeResponseDto::class)
            .challenge
        assertTrue(challengeBeforeComplete.isComplete.not())
        contributePlaceAccessibility(
            user = users.random(),
            challenge = challenge
        )
        val challengeAfterComplete = mvc.sccRequest(
            "/getChallenge",
            GetChallengeRequestDto(
                challengeId = challenge.id
            ),
            user = users.first()
        )
            .getResult(GetChallengeResponseDto::class)
            .challenge
        assertTrue(challengeAfterComplete.isComplete)
    }

    // TODO: 랭킹 구현 후 테스트 구현

//    @Test
//    fun `진행 중인 챌린지에 참여하지 않았다면 다른 사람들의 랭킹이 보이지 않는다`() {
//        val inProgressChallenge = registerInProgressChallenge()
//        val user = testDataGenerator.createUser()
//
//        val getChallengeResponseDto = mvc.sccRequest(
//            "/getChallenge",
//            GetChallengeRequestDto(
//                challengeId = inProgressChallenge.id
//            ),
//            user = user
//        )
//            .getResult(GetChallengeResponseDto::class)
//    }

//    @Test
//    fun `진행 중인 챌린지에 참여 중이면 내 랭킹과 다른 사람들의 랭킹까지 보여준다`() {
//        val inProgressChallenge = registerInProgressChallenge()
//        val user = testDataGenerator.createUser()
//
//        val getChallengeResponseDto = mvc.sccRequest(
//            "/getChallenge",
//            GetChallengeRequestDto(
//                challengeId = inProgressChallenge.id
//            ),
//            user = user
//        )
//            .getResult(GetChallengeResponseDto::class)
//    }

//    @Test
//    fun `종료된 챌린지에 참여하지 않았다면 다른 사람들의 랭킹만 보여준다`() {
//        val closedChallenge = registerClosedChallenge()
//        val user = testDataGenerator.createUser()
//
//        val getChallengeResponseDto = mvc.sccRequest(
//            "/getChallenge",
//            GetChallengeRequestDto(
//                challengeId = closedChallenge.id
//            ),
//            user = user
//        )
//            .getResult(GetChallengeResponseDto::class)
//    }

//    @Test
//    fun `종료된 챌린지에 이미 참여했었다면 내 랭킹과 다른 사람들의 랭킹을 보여준다`() {
//        val closedChallenge = registerClosedChallenge()
//        val user = testDataGenerator.createUser()
//
//        val getChallengeResponseDto = mvc.sccRequest(
//            "/getChallenge",
//            GetChallengeRequestDto(
//                challengeId = closedChallenge.id
//            ),
//            user = user
//        )
//            .getResult(GetChallengeResponseDto::class)
//    }
}
