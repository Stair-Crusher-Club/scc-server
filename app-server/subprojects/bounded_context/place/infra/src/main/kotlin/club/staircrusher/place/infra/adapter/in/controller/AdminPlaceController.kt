package club.staircrusher.place.infra.adapter.`in`.controller

import club.staircrusher.admin_api.converter.toModel
import club.staircrusher.admin_api.spec.dto.AdminClosedPlaceCandidateDTO
import club.staircrusher.admin_api.spec.dto.AdminListClosedPlaceCandidatesResponseDTO
import club.staircrusher.admin_api.spec.dto.StartPlaceCrawlingRequestDTO
import club.staircrusher.place.application.port.`in`.AcceptClosedPlaceCandidateUseCase
import club.staircrusher.place.application.port.`in`.GetClosedPlaceCandidateUseCase
import club.staircrusher.place.application.port.`in`.IgnoreClosedPlaceCandidateUseCase
import club.staircrusher.place.application.port.`in`.ListClosedPlaceCandidatesUseCase
import club.staircrusher.place.application.port.`in`.StartPlaceCrawlingUseCase
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class AdminPlaceController(
    private val startPlaceCrawlingUseCase: StartPlaceCrawlingUseCase,
    private val listClosedPlaceCandidatesUseCase: ListClosedPlaceCandidatesUseCase,
    private val getClosedPlaceCandidateUseCase: GetClosedPlaceCandidateUseCase,
    private val acceptClosedPlaceCandidateUseCase: AcceptClosedPlaceCandidateUseCase,
    private val ignoreClosedPlaceCandidateUseCase: IgnoreClosedPlaceCandidateUseCase,
) {
    @PostMapping("/admin/places/startCrawling")
    fun startPlaceCrawling(@RequestBody request: StartPlaceCrawlingRequestDTO) {
        startPlaceCrawlingUseCase.handle(request.boundaryVertices.map { it.toModel() })
    }

    @GetMapping("/admin/closedPlaceCandidates")
    fun listClosedPlaceCandidates(
        @RequestParam(required = false) limit: Int?,
        @RequestParam(required = false) cursor: String?,
    ): AdminListClosedPlaceCandidatesResponseDTO {
        return listClosedPlaceCandidatesUseCase.handle(
            limit = limit,
            cursorValue = cursor,
        ).run {
            AdminListClosedPlaceCandidatesResponseDTO(
                list = candidates.map { it.toAdminDTO() },
                cursor = nextCursor,
            )
        }
    }

    @GetMapping("/admin/closedPlaceCandidates/{candidateId}")
    fun getClosedPlaceCandidate(@PathVariable candidateId: String): AdminClosedPlaceCandidateDTO {
        return getClosedPlaceCandidateUseCase.handle(candidateId)?.toAdminDTO()
            ?: throw IllegalArgumentException("closed place candidate with id($candidateId) not found")
    }

    @PutMapping("/admin/closedPlaceCandidates/{candidateId}/accept")
    fun acceptClosedPlaceCandidate(@PathVariable candidateId: String): AdminClosedPlaceCandidateDTO {
        return acceptClosedPlaceCandidateUseCase.handle(candidateId).toAdminDTO()
    }

    @PutMapping("/admin/closedPlaceCandidates/{candidateId}/ignore")
    fun ignoreClosedPlaceCandidate(@PathVariable candidateId: String): AdminClosedPlaceCandidateDTO {
        return ignoreClosedPlaceCandidateUseCase.handle(candidateId).toAdminDTO()
    }
}
