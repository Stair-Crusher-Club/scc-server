package club.staircrusher.challenge.infra.adapter.`in`.controller

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
    fun `이미 참여하고 있지 않은 챌린지에 참여 요청 시 참여 완료`() {
    }

    @Test
    fun `이미 참여한 챌린지는 이미 참여했음을 알리는 에러가 난다`() {
    }

    @Test
    fun `참여 코드가 필요한 챌린지에 참여 코드를 알맞게 입력하면 참여 완료`() {
    }

    @Test
    fun `참여 코드가 필요한 챌린지에 참여코드가 없다면 관련된 에러가 난다`() {
    }

    @Test
    fun `참여 코드가 필요한 챌린지에 참여코드를 맞게 입력하지 않으면 관련된 에러가 난다`() {
    }

    @Test
    fun `종료되거나 오픈예정인 챌린지에는 참여할 수 없다`() {
    }
}
