package club.staircrusher.place.infra.adapter.`in`.controller.search

import club.staircrusher.admin_api.spec.dto.AdminCreateSearchPlacePresetRequestDTO
import club.staircrusher.admin_api.spec.dto.GetSearchPreset200Response
import club.staircrusher.place.application.port.`in`.search.SearchPlacePresetService
import club.staircrusher.spring_web.security.admin.SccAdminAuthentication
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AdminSearchPlacesController(
    private val searchPlacePresetService: SearchPlacePresetService,
) {

    @GetMapping("/admin/places/searchPreset")
    fun getSearchPresets(
        @Suppress("UnusedPrivateMember") authentication: SccAdminAuthentication,
    ) : GetSearchPreset200Response {
        return GetSearchPreset200Response(
            presets = searchPlacePresetService.list().map { it.toAdminDTO() }
        )
    }

    @PostMapping("/admin/places/searchPreset")
    fun createSearchPreset(
        @RequestBody request: AdminCreateSearchPlacePresetRequestDTO,
        @Suppress("UnusedPrivateMember") authentication: SccAdminAuthentication,
    ) : ResponseEntity<Unit> {
        searchPlacePresetService.createKeywordPreset(
            description = request.description,
            searchText = request.searchText
        )

        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/admin/places/searchPreset/{id}")
    fun deleteSearchPreset(
        @PathVariable id: String,
        @Suppress("UnusedPrivateMember") authentication: SccAdminAuthentication,
    ) : ResponseEntity<Unit> {
        searchPlacePresetService.deleteById(id)

        return ResponseEntity.noContent().build()
    }
}
