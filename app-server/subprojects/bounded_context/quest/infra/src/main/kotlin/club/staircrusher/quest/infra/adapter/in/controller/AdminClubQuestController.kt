package club.staircrusher.quest.infra.adapter.`in`.controller

import club.staircrusher.admin_api.converter.toModel
import club.staircrusher.admin_api.spec.dto.ClubQuestCreateDryRunResultItemDTO
import club.staircrusher.admin_api.spec.dto.ClubQuestDTO
import club.staircrusher.admin_api.spec.dto.ClubQuestsClubQuestIdIsClosedPutRequest
import club.staircrusher.admin_api.spec.dto.ClubQuestsClubQuestIdIsNotAccessiblePutRequest
import club.staircrusher.admin_api.spec.dto.ClubQuestsCreateDryRunPostRequest
import club.staircrusher.admin_api.spec.dto.ClubQuestsGet200ResponseInner
import club.staircrusher.admin_api.spec.dto.CreateAndNotifyDailyClubQuestRequestDTO
import club.staircrusher.admin_api.spec.dto.CreateAndNotifyDailyClubQuestResponseDTO
import club.staircrusher.admin_api.spec.dto.CreateClubQuestRequest
import club.staircrusher.admin_api.spec.dto.CreateClubQuestResponseDTO
import club.staircrusher.admin_api.spec.dto.GetCursoredClubQuestSummariesResultDTO
import club.staircrusher.quest.application.port.`in`.ClubQuestCreateAplService
import club.staircrusher.quest.application.port.`in`.ClubQuestSetIsClosedUseCase
import club.staircrusher.quest.application.port.`in`.ClubQuestSetIsNotAccessibleUseCase
import club.staircrusher.quest.application.port.`in`.CreateAndNotifyDailyClubQuestUseCase
import club.staircrusher.quest.application.port.`in`.CrossValidateClubQuestPlacesUseCase
import club.staircrusher.quest.application.port.`in`.DeleteClubQuestUseCase
import club.staircrusher.quest.application.port.`in`.GetClubQuestUseCase
import club.staircrusher.quest.application.port.`in`.GetCursoredClubQuestSummariesUseCase
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class AdminClubQuestController(
    private val clubQuestCreateAplService: ClubQuestCreateAplService,
    private val getClubQuestUseCase: GetClubQuestUseCase,
    private val clubQuestSetIsClosedUseCase: ClubQuestSetIsClosedUseCase,
    private val clubQuestSetIsNotAccessibleUseCase: ClubQuestSetIsNotAccessibleUseCase,
    private val deleteClubQuestUseCase: DeleteClubQuestUseCase,
    private val clubQuestRepository: ClubQuestRepository,
    private val crossValidateClubQuestPlacesUseCase: CrossValidateClubQuestPlacesUseCase,
    private val getCursoredClubQuestSummariesUseCase: GetCursoredClubQuestSummariesUseCase,
    private val createAndNotifyDailyClubQuestUseCase: CreateAndNotifyDailyClubQuestUseCase,
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

    @GetMapping("/admin/clubQuestSummaries/cursored")
    fun getCursoredClubQuestSummaries(
        @RequestParam(required = false) limit: Int?,
        @RequestParam(required = false) cursor: String?,
    ): GetCursoredClubQuestSummariesResultDTO {
        return getCursoredClubQuestSummariesUseCase.handle(
            limit = limit,
            cursorValue = cursor
        ).run {
            GetCursoredClubQuestSummariesResultDTO(
                list = list.map { it.toDTO() },
                cursor = nextCursor,
            )
        }
    }

    @PostMapping("/admin/clubQuests/create/dryRun")
    suspend fun createClubQuestDryRun(@RequestBody request: ClubQuestsCreateDryRunPostRequest): List<ClubQuestCreateDryRunResultItemDTO> {
        val result = clubQuestCreateAplService.createDryRun(
            regionType = request.regionType?.toModel(),
            centerLocation = request.centerLocation?.toModel(),
            radiusMeters = request.radiusMeters,
            points = request.points?.map { it.toModel() },
            clusterCount = request.clusterCount,
            maxPlaceCountPerQuest = request.maxPlaceCountPerQuest,
        )
        return result.map { it.toDTO(conqueredPlaceIds = emptySet()) }
    }

    @PostMapping("/admin/clubQuests/create")
    fun createClubQuest(@RequestBody request: CreateClubQuestRequest): CreateClubQuestResponseDTO {
        val quests = clubQuestCreateAplService.createFromDryRunResult(
            questNamePrefix = request.questNamePrefix,
            dryRunResultItems = request.dryRunResults.map { it.toModel() }
        )
        quests.forEach { quest ->
            crossValidateClubQuestPlacesUseCase.handleAsync(quest.id)
        }
        return CreateClubQuestResponseDTO(
            clubQuestIds = quests.map { it.id },
        )
    }

    @PostMapping("/admin/clubQuests/createAndNotifyDailyClubQuest")
    fun createAndNotifyDailyClubQuest(@RequestBody request: CreateAndNotifyDailyClubQuestRequestDTO): CreateAndNotifyDailyClubQuestResponseDTO {
        val maxPlaceCountPerQuest = request.maxPlaceCountPerQuest
            .replace(Regex("개.*"), "")
            .toIntOrNull()
            ?: throw IllegalArgumentException("maxPlaceCountPerQuest(${request.maxPlaceCountPerQuest})를 숫자로 변환할 수 없습니다.")
        val result = createAndNotifyDailyClubQuestUseCase.handle(
            requesterName = request.requesterName,
            requesterPhoneNumber = request.requesterPhoneNumber,
            centerLocationPlaceName = request.centerLocationPlaceName,
            maxPlaceCountPerQuest = maxPlaceCountPerQuest,
        )

        crossValidateClubQuestPlacesUseCase.handleAsync(result.clubQuest.id)
        return CreateAndNotifyDailyClubQuestResponseDTO(
            clubQuestId = result.clubQuest.id,
            url = result.url,
        )
    }

    @GetMapping("/admin/clubQuests/{clubQuestId}")
    fun getClubQuest(@PathVariable clubQuestId: String): ClubQuestDTO {
        return getClubQuestUseCase.handle(clubQuestId).toDTO()
    }

    @DeleteMapping("/admin/clubQuests/{clubQuestId}")
    fun deleteClubQuest(@PathVariable clubQuestId: String): ResponseEntity<Unit> {
        deleteClubQuestUseCase.handle(clubQuestId)
        return ResponseEntity
            .noContent()
            .build()
    }

    @PutMapping("/admin/clubQuests/{clubQuestId}/isClosed")
    fun setIsClosed(
        @Suppress("UnusedPrivateMember") @PathVariable clubQuestId: String,
        @RequestBody request: ClubQuestsClubQuestIdIsClosedPutRequest
    ): ResponseEntity<Unit> {
        clubQuestSetIsClosedUseCase.handle(
            request.placeId,
            request.isClosed,
        )

        return ResponseEntity.noContent().build()
    }

    @PutMapping("/admin/clubQuests/{clubQuestId}/isNotAccessible")
    fun setIsNotAccessible(
        @Suppress("UnusedPrivateMember") @PathVariable clubQuestId: String,
        @RequestBody request: ClubQuestsClubQuestIdIsNotAccessiblePutRequest
    ): ResponseEntity<Unit> {
        clubQuestSetIsNotAccessibleUseCase.handle(
            request.placeId,
            request.isNotAccessible,
        )

        return ResponseEntity.noContent().build()
    }

    @PutMapping("/admin/clubQuests/{clubQuestId}/crossValidate")
    fun crossValidate(@PathVariable clubQuestId: String) {
        return crossValidateClubQuestPlacesUseCase.handleAsync(clubQuestId)
    }
}
