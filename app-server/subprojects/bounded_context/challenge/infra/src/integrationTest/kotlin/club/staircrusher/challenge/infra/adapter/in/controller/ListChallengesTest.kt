package club.staircrusher.challenge.infra.adapter.`in`.controller

import club.staircrusher.challenge.application.port.out.persistence.ChallengeContributionRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeParticipationRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeRepository
import club.staircrusher.challenge.infra.adapter.`in`.controller.base.ChallengeITBase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ListChallengesTest : ChallengeITBase() {
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
    fun `진행 중, 오픈 예정, 종료 순으로 내려준다`() {
    }

    @Test
    fun `필터가 있는 경우 그에 해당하는 챌린지만 내려온다`() {
    }

    @Test
    fun `필터가 여러 개 있는 경우 여러 필터에 해당하는 챌린지만 내려온다`() {
    }

    @Test
    fun `진행 중 & 참여 중 챌린지는 앞쪽에 참여한 순서대로 차례대로 나온다`() {
    }
}
