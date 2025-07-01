package club.staircrusher.place.infra.adapter.`in`.controller.search

import club.staircrusher.admin_api.spec.dto.AdminSearchPlacePresetDTO
import club.staircrusher.place.domain.model.search.SearchPlacePreset

fun SearchPlacePreset.toAdminDTO() = AdminSearchPlacePresetDTO(
    id = id,
    description = description,
    searchText = searchText,
)
