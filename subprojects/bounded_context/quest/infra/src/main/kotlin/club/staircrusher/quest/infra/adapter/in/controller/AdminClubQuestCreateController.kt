package club.staircrusher.quest.infra.adapter.`in`.controller

import club.staircrusher.admin.api.dto.ClubQuestCreateDryRunResultItemDTO
import club.staircrusher.admin.api.dto.ClubQuestsCreateDryRunPostRequest
import club.staircrusher.admin.api.dto.ClubQuestsCreatePostRequest
import club.staircrusher.quest.application.port.`in`.ClubQuestCreateAplService
import club.staircrusher.quest.infra.adapter.`in`.converter.toDTO
import club.staircrusher.quest.infra.adapter.`in`.converter.toModel
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AdminClubQuestCreateController(
    private val clubQuestCreateAplService: ClubQuestCreateAplService,
) {
    @PostMapping("/admin/clubQuests/create/dryRun")
    fun createDryRun(@RequestBody request: ClubQuestsCreateDryRunPostRequest): List<ClubQuestCreateDryRunResultItemDTO> {
        val result = clubQuestCreateAplService.createDryRun(
            centerLocation = request.centerLocation.toModel(),
            radiusMeters = request.radiusMeters,
            clusterCount = request.clusterCount,
        )
        return result.map { it.toDTO(conqueredPlaceIds = emptySet()) } // TODO: conqueredPlaceIds 제대로 채우기
    }

    @PostMapping("/admin/clubQuests/create")
    fun create(@RequestBody request: ClubQuestsCreatePostRequest): ResponseEntity<Unit> {
        clubQuestCreateAplService.createFromDryRunResult(
            questNamePrefix = request.questNamePrefix,
            dryRunResultItems = request.dryRunResults.map { it.toModel() }
        )
        return ResponseEntity
            .noContent()
            .build()
    }
}
