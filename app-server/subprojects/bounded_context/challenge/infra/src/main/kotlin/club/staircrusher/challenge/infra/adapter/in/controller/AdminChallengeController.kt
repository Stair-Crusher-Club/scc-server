package club.staircrusher.challenge.infra.adapter.`in`.controller

import club.staircrusher.admin_api.spec.dto.AdminChallengeDTO
import club.staircrusher.admin_api.spec.dto.AdminCreateChallengeRequestDTO
import club.staircrusher.challenge.application.port.`in`.use_case.CreateChallengeUseCase
import club.staircrusher.challenge.application.port.`in`.use_case.DeleteChallengeUseCase
import club.staircrusher.challenge.application.port.`in`.use_case.GetChallengeUseCase
import club.staircrusher.challenge.application.port.`in`.use_case.ListAllChallengesUseCase
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AdminChallengeController(
    private val listAllChallengesUseCase: ListAllChallengesUseCase,
    private val getChallengeUseCase: GetChallengeUseCase,
    private val createChallengeUseCase: CreateChallengeUseCase,
    private val deleteChallengeUseCase: DeleteChallengeUseCase,
) {
    @GetMapping("/admin/challenges")
    fun listAllChallenges(): List<AdminChallengeDTO> {
        return listAllChallengesUseCase.handle()
            .map { it.toAdminDTO() }
    }

    @GetMapping("/admin/challenges/{challengeId}")
    fun listAllChallenges(@PathVariable challengeId: String): AdminChallengeDTO {
        return getChallengeUseCase.handle(userId = null, challengeId = challengeId).challenge.toAdminDTO()
    }

    @PostMapping("/admin/challenges")
    fun createChallenge(@RequestBody request: AdminCreateChallengeRequestDTO) {
        createChallengeUseCase.handle(request.toModel())
    }

    @DeleteMapping("/admin/challenges/{challengeId}")
    fun deleteChallenge(@PathVariable challengeId: String) {
        deleteChallengeUseCase.handle(challengeId)
    }
}
