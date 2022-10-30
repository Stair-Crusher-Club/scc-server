package club.staircrusher.quest.infra.adapter.`in`.controller

import club.staircrusher.admin.api.dto.ClubQuestCreateDryRunResultItemDTO
import club.staircrusher.admin.api.dto.ClubQuestDTO
import club.staircrusher.admin.api.dto.ClubQuestsClubQuestsIdIsClosedPutRequest
import club.staircrusher.admin.api.dto.ClubQuestsClubQuestsIdIsNotAccessiblePutRequest
import club.staircrusher.admin.api.dto.ClubQuestsCreateDryRunPostRequest
import club.staircrusher.admin.api.dto.ClubQuestsCreatePostRequest
import club.staircrusher.admin.api.dto.ClubQuestsGet200ResponseInner
import club.staircrusher.quest.application.port.`in`.ClubQuestCreateAplService
import club.staircrusher.quest.application.port.`in`.ClubQuestSetIsClosedUseCase
import club.staircrusher.quest.application.port.`in`.ClubQuestSetIsNotAccessibleUseCase
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
        return result.map { it.toDTO(conqueredPlaceIds = emptySet()) } // TODO: conqueredPlaceIds 제대로 채우기
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
        return clubQuestRepository.findById(clubQuestId).toDTO(conqueredPlaceIds = emptySet()) // TODO: conqueredPlaceIds 제대로 채우기
    }

    @DeleteMapping("/admin/clubQuests/{clubQuestId}")
    fun deleteClubQuest(@PathVariable clubQuestId: String): ResponseEntity<Unit> {
        clubQuestRepository.remove(clubQuestId)
        return ResponseEntity
            .noContent()
            .build()
    }

    @PutMapping("/admin/clubQuests/{clubQuestId}/isClosed")
    fun setIsClosed(@PathVariable clubQuestId: String, @RequestBody request: ClubQuestsClubQuestsIdIsClosedPutRequest): ClubQuestDTO {
        return clubQuestSetIsClosedUseCase.handle(
            clubQuestId,
            request.buildingId,
            request.placeId,
            request.isClosed,
        ).toDTO(conqueredPlaceIds = emptySet()) // TODO: conqueredPlaceIds 제대로 채우기
    }

    @PutMapping("/admin/clubQuests/{clubQuestId}/isNotAccessible")
    fun setIsNotAccessible(@PathVariable clubQuestId: String, @RequestBody request: ClubQuestsClubQuestsIdIsNotAccessiblePutRequest): ClubQuestDTO {
        return clubQuestSetIsNotAccessibleUseCase.handle(
            clubQuestId,
            request.buildingId,
            request.placeId,
            request.isNotAccessible,
        ).toDTO(conqueredPlaceIds = emptySet()) // TODO: conqueredPlaceIds 제대로 채우기
    }
}
