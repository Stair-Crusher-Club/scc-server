package club.staircrusher.place.infra.adapter.`in`.controller.accessibility

import club.staircrusher.api.spec.dto.GetAccessibilityLeaderboardPost200Response
import club.staircrusher.api.spec.dto.GetAccessibilityRankPost200Response
import club.staircrusher.api.spec.dto.GetCountForNextRankPost200Response
import club.staircrusher.place.application.port.`in`.accessibility.GetCountForNextRankUseCase
import club.staircrusher.place.application.port.`in`.accessibility.GetLeaderboardUseCase
import club.staircrusher.place.application.port.`in`.accessibility.GetRankUseCase
import club.staircrusher.place.application.port.`in`.accessibility.UpdateRanksUseCase
import club.staircrusher.spring_web.security.InternalIpAddressChecker
import club.staircrusher.spring_web.security.app.SccAppAuthentication
import club.staircrusher.stdlib.di.annotation.Component
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Component
class AccessibilityRankController(
    private val getCountForNextRankUseCase: GetCountForNextRankUseCase,
    private val getRankUseCase: GetRankUseCase,
    private val updateRanksUseCase: UpdateRanksUseCase,
    private val getLeaderboardUseCase: GetLeaderboardUseCase,
) {
    @PostMapping("/getAccessibilityRank")
    fun getAccessibilityRank(
        authentication: SccAppAuthentication,
    ): GetAccessibilityRankPost200Response {
        return GetAccessibilityRankPost200Response(
            accessibilityRank = getRankUseCase.handle(authentication.principal).let { (rank, user)  ->
                rank.toDTO(user!!)
            }
        )
    }

    @PostMapping("/getAccessibilityLeaderboard")
    fun getAccessibilityLeaderboard(): GetAccessibilityLeaderboardPost200Response {
        return GetAccessibilityLeaderboardPost200Response(
            ranks = getLeaderboardUseCase.handle().map { (rank, user) ->
                rank.toDTO(user!!)
            }
        )
    }

    @PostMapping("/getCountForNextRank")
    fun getCountForNextRank(
        authentication: SccAppAuthentication,
    ): GetCountForNextRankPost200Response {
        return GetCountForNextRankPost200Response(
            countForNextRank = getCountForNextRankUseCase.handle(authentication.principal)
        )
    }

    @PostMapping("/updateAccessibilityRanks")
    fun updateAccessibilityRanks(request: HttpServletRequest) {
        InternalIpAddressChecker.check(request)
        updateRanksUseCase.handle()
    }
}
