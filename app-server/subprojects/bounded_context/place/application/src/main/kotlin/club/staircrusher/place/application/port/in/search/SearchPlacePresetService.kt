package club.staircrusher.place.application.port.`in`.search

import club.staircrusher.place.application.port.out.search.persistence.SearchPlacePresetRepository
import club.staircrusher.place.domain.model.search.SearchPlacePreset
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator

@Component
class SearchPlacePresetService(
    private val searchPlacePresetRepository: SearchPlacePresetRepository,
) {
    fun list(): List<SearchPlacePreset> {
        return searchPlacePresetRepository.findAll().toList()
    }

    fun createKeywordPreset(description: String, searchText: String): SearchPlacePreset {
        val preset = SearchPlacePreset.ofKeyword(
            id = EntityIdGenerator.generateRandom(),
            description = description,
            searchText = searchText
        )
        return searchPlacePresetRepository.save(preset)
    }

    fun deleteById(id: String) {
        searchPlacePresetRepository.deleteById(id)
    }
}
