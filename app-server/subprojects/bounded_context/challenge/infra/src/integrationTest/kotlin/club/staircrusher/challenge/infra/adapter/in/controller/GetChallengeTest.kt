package club.staircrusher.challenge.infra.adapter.`in`.controller

import club.staircrusher.challenge.application.port.out.persistence.ChallengeContributionRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeParticipationRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeRepository
import club.staircrusher.challenge.infra.adapter.`in`.controller.base.ChallengeITBase
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class GetChallengeTest : ChallengeITBase() {
    @Autowired
    private lateinit var challengeRepository: ChallengeRepository

    @Autowired
    private lateinit var challengeContributionRepository: ChallengeContributionRepository

    @Autowired
    private lateinit var challengeParticipationRepository: ChallengeParticipationRepository

    @Test
    fun `참여자 수, 정복한 장소가 맞는지 확인한다`() {
    }

    @Test
    fun `정복한 장소가 목표치에 도달하면 완료 상태를 만든다`() {
    }

    @Test
    fun `진행 중인 챌린지에 참여하지 않았다면 다른 사람들의 랭킹이 보이지 않는다`() {
    }

    @Test
    fun `진행 중인 챌린지에 참여 중이면 내 랭킹과 다른 사람들의 랭킹까지 보여준다`() {
    }

    @Test
    fun `종료된 챌린지에 참여하지 않았다면 다른 사람들의 랭킹만 보여준다`() {
    }

    @Test
    fun `종료된 챌린지에 이미 참여했었다면 내 랭킹과 다른 사람들의 랭킹을 보여준다`() {
    }
}
