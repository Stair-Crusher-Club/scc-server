package club.staircrusher.quest.infra.adapter.`in`.controller

import club.staircrusher.admin_api.converter.toModel
import club.staircrusher.admin_api.spec.dto.ClubQuestCreateDryRunResultItemDTO
import club.staircrusher.admin_api.spec.dto.ClubQuestDTO
import club.staircrusher.admin_api.spec.dto.ClubQuestsClubQuestIdIsClosedPutRequest
import club.staircrusher.admin_api.spec.dto.ClubQuestsClubQuestIdIsNotAccessiblePutRequest
import club.staircrusher.admin_api.spec.dto.ClubQuestsCreateDryRunPostRequest
import club.staircrusher.admin_api.spec.dto.ClubQuestsCreatePostRequest
import club.staircrusher.admin_api.spec.dto.ClubQuestsGet200ResponseInner
import club.staircrusher.quest.application.port.`in`.ClubQuestCreateAplService
import club.staircrusher.quest.application.port.`in`.ClubQuestSetIsClosedUseCase
import club.staircrusher.quest.application.port.`in`.ClubQuestSetIsNotAccessibleUseCase
import club.staircrusher.quest.application.port.`in`.GetClubQuestUseCase
import club.staircrusher.quest.application.port.out.persistence.ClubQuestRepository
import club.staircrusher.quest.infra.adapter.`in`.converter.toDTO
import club.staircrusher.quest.infra.adapter.`in`.converter.toModel
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AdminClubQuestController(
    private val clubQuestCreateAplService: ClubQuestCreateAplService,
    private val getClubQuestUseCase: GetClubQuestUseCase,
    private val clubQuestSetIsClosedUseCase: ClubQuestSetIsClosedUseCase,
    private val clubQuestSetIsNotAccessibleUseCase: ClubQuestSetIsNotAccessibleUseCase,
    private val clubQuestRepository: ClubQuestRepository,
) {
    @GetMapping("/admin/clubQuests")
    fun listClubQuests(): List<ClubQuestsGet200ResponseInner> {
        return clubQuestRepository.findAllOrderByCreatedAtDesc().map {
            ClubQuestsGet200ResponseInner(
                id = it.id,
                name = it.name,
            )
        }
    }

    @PostMapping("/admin/clubQuests/create/dryRun")
    fun createClubQuestDryRun(@RequestBody request: ClubQuestsCreateDryRunPostRequest): List<ClubQuestCreateDryRunResultItemDTO> {
        val result = clubQuestCreateAplService.createDryRun(
            centerLocation = request.centerLocation.toModel(),
            radiusMeters = request.radiusMeters,
            clusterCount = request.clusterCount,
        )
        return result.map { it.toDTO(conqueredPlaceIds = emptySet()) }
    }

    @PostMapping("/admin/clubQuests/create")
    fun createClubQuest(@RequestBody request: ClubQuestsCreatePostRequest): ResponseEntity<Unit> {
        clubQuestCreateAplService.createFromDryRunResult(
            questNamePrefix = request.questNamePrefix,
            dryRunResultItems = request.dryRunResults.map { it.toModel() }
        )
        return ResponseEntity
            .noContent()
            .build()
    }

    @GetMapping("/admin/clubQuests/{clubQuestId}")
    fun getClubQuest(@PathVariable clubQuestId: String): ClubQuestDTO {
        return getClubQuestUseCase.handle(clubQuestId).toDTO()
    }

    @DeleteMapping("/admin/clubQuests/{clubQuestId}")
    fun deleteClubQuest(@PathVariable clubQuestId: String): ResponseEntity<Unit> {
        clubQuestRepository.remove(clubQuestId)
        return ResponseEntity
            .noContent()
            .build()
    }

    @PutMapping("/admin/clubQuests/{clubQuestId}/isClosed")
    fun setIsClosed(@PathVariable clubQuestId: String, @RequestBody request: ClubQuestsClubQuestIdIsClosedPutRequest): ClubQuestDTO {
        return clubQuestSetIsClosedUseCase.handle(
            clubQuestId,
            request.buildingId,
            request.placeId,
            request.isClosed,
        ).toDTO()
    }

    @PutMapping("/admin/clubQuests/{clubQuestId}/isNotAccessible")
    fun setIsNotAccessible(@PathVariable clubQuestId: String, @RequestBody request: ClubQuestsClubQuestIdIsNotAccessiblePutRequest): ClubQuestDTO {
        return clubQuestSetIsNotAccessibleUseCase.handle(
            clubQuestId,
            request.buildingId,
            request.placeId,
            request.isNotAccessible,
        ).toDTO()
    }
}
