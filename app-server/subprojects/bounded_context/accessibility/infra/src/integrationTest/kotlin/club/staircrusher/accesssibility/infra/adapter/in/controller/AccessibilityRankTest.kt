package club.staircrusher.accesssibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityRepository
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityUpvoteRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.accesssibility.infra.adapter.`in`.controller.base.AccessibilityITBase
import club.staircrusher.api.spec.dto.GetAccessibilityLeaderboardPost200Response
import club.staircrusher.api.spec.dto.GetAccessibilityRankPost200Response
import club.staircrusher.api.spec.dto.GetCountForNextRankPost200Response
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.random.Random

class AccessibilityRankTest : AccessibilityITBase() {
    @Autowired
    private lateinit var placeAccessibilityRepository: PlaceAccessibilityRepository
    @Autowired
    private lateinit var buildingAccessibilityRepository: BuildingAccessibilityRepository
    @Autowired
    private lateinit var buildingAccessibilityUpvoteRepository: BuildingAccessibilityUpvoteRepository

    @BeforeEach
    fun setUp() = transactionManager.doInTransaction {
        placeAccessibilityRepository.removeAll()
        buildingAccessibilityUpvoteRepository.removeAll()
        buildingAccessibilityRepository.removeAll()

        repeat(10) {
            val (user, _, _, _) = registerAccessibility()
            val random = Random.nextInt(100)
            repeat(random) {
                registerAccessibility(overridingUser = user)
            }
        }
        mvc.sccRequest("/updateAccessibilityRanks", null)
        Unit
    }

    /**
     * Test accessibility rank calculation, first insert some accessibility data
     * to databases and update rank status and get leaderboard.
     */
    @Test
    fun `leaderboard use case test`() {
        mvc
            .sccRequest("/getAccessibilityLeaderboard", null)
            .apply {
                val result = getResult(GetAccessibilityLeaderboardPost200Response::class)
                assertEquals(10, result.ranks.size)

                result.ranks.forEach { rank -> assertTrue(rank.rank <= 10) }

                val top = result.ranks.find { it.rank == 1L }!!
                result.ranks.forEach { assertTrue(top.conqueredCount >= it.conqueredCount) }
            }
    }

    /**
     * test accessibility rank calculation when there are same conquered count
     */
    @Test
    fun `same conquered count case test`() {
        val leaderboard = mvc
            .sccRequest("/getAccessibilityLeaderboard", null)
            .getResult(GetAccessibilityLeaderboardPost200Response::class)

        val top = leaderboard.ranks.find { it.rank == 1L }!!
        val (user, _, _, _) = registerAccessibility()
        repeat(top.conqueredCount - 1) { registerAccessibility(overridingUser = user) }
        mvc.sccRequest("/updateAccessibilityRanks", null)

        val rank = mvc
            .sccRequest("/getAccessibilityRank", null, user = user)
            .getResult(GetAccessibilityRankPost200Response::class)
            .accessibilityRank
        assertEquals(1L, rank.rank)

        val leaderboard2 = mvc
            .sccRequest("/getAccessibilityLeaderboard", null)
            .getResult(GetAccessibilityLeaderboardPost200Response::class)
        val secondRanker = leaderboard.ranks.first { it.rank != 1L }
        val secondRanker2 = leaderboard2.ranks.first { it.rank != 1L }
        assertEquals(secondRanker.user, secondRanker2.user)
        assertEquals(secondRanker.rank + 1, secondRanker2.rank)
    }

    /**
     * test next rank use case
     */
    @Test
    fun `get next rank`() {
        val (user, _, _, _) = registerAccessibility()
        repeat(10) { registerAccessibility(overridingUser = user) }
        mvc.sccRequest("/updateAccessibilityRanks", null)

        val countForNextRank = mvc
            .sccRequest("/getCountForNextRank", null, user = user)
            .getResult(GetCountForNextRankPost200Response::class)
            .countForNextRank

        assertTrue(countForNextRank > 0)
    }
}
