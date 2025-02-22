package club.staircrusher.challenge.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.GetChallengeRequestDto
import club.staircrusher.api.spec.dto.GetChallengeResponseDto
import club.staircrusher.challenge.application.port.`in`.use_case.HandlePlaceAccessibilityDeletedEventUseCase
import club.staircrusher.challenge.application.port.out.persistence.ChallengeContributionRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeParticipationRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeRepository
import club.staircrusher.challenge.infra.adapter.`in`.controller.base.ChallengeITBase
import club.staircrusher.domain_event.PlaceAccessibilityDeletedEvent
import club.staircrusher.domain_event.dto.AccessibilityRegistererDTO
import club.staircrusher.place.application.port.`in`.toPlaceDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ChallengeContributionDeletionTest : ChallengeITBase() {
    @Autowired
    private lateinit var challengeRepository: ChallengeRepository

    @Autowired
    private lateinit var challengeContributionRepository: ChallengeContributionRepository

    @Autowired
    private lateinit var challengeParticipationRepository: ChallengeParticipationRepository

    @Autowired
    private lateinit var handlePlaceAccessibilityDeletedEventUseCase: HandlePlaceAccessibilityDeletedEventUseCase

    @BeforeEach
    fun setUp() {
        transactionManager.doInTransaction {
            challengeRepository.deleteAll()
            challengeParticipationRepository.deleteAll()
            challengeContributionRepository.deleteAll()

            registerChallenges()
        }
    }

    @Test
    fun `장소 정보 삭제 시 contribution이 삭제된다`() {
        // given
        val challenge = registerInProgressChallenge()
        val user = transactionManager.doInTransaction {
            val user = testDataGenerator.createIdentifiedUser()
            participate(user.account, challenge)
            user
        }
        val place = transactionManager.doInTransaction {
            testDataGenerator.createBuildingAndPlace(placeName = "장소장소", building = null)
        }
        val contributions = (0 until 5).map {
            contributePlaceAccessibility(
                userAccount = user.account,
                overridingPlace = place,
                challenge = challenge,
            )
        }
        val getChallengeResponse = mvc.sccRequest(
            "/getChallenge",
            GetChallengeRequestDto(
                challengeId = challenge.id,
            ),
            userAccount = user.account,
        )
            .getResult(GetChallengeResponseDto::class)
        assertEquals(challenge.id, getChallengeResponse.challenge.id)
        assertEquals(contributions.count(), getChallengeResponse.challenge.contributionsCount)

        // when
        val contributionToDelete = contributions[0]
        handlePlaceAccessibilityDeletedEventUseCase.handle(
            event = PlaceAccessibilityDeletedEvent(
                id = contributionToDelete.placeAccessibilityId!!,
                accessibilityRegisterer = AccessibilityRegistererDTO(
                    id = contributionToDelete.userId,
                ),
                place = place.toPlaceDTO(),
            ),
        )

        // then
        val getChallengeResponse2 = mvc.sccRequest(
            "/getChallenge",
            GetChallengeRequestDto(
                challengeId = challenge.id,
            ),
            userAccount = user.account,
        )
            .getResult(GetChallengeResponseDto::class)
        assertEquals(challenge.id, getChallengeResponse2.challenge.id)
        assertEquals(contributions.count() - 1, getChallengeResponse2.challenge.contributionsCount)

        val remainingContributions = transactionManager.doInTransaction {
            challengeContributionRepository.findByChallengeId(challengeId = challenge.id)
        }
        assertEquals(contributions.count() - 1, remainingContributions.size)
        assertTrue(contributionToDelete.id !in remainingContributions.map { it.id })
    }
}
