package club.staircrusher.place.application.port.`in`.search

import club.staircrusher.place.application.port.out.search.persistence.SearchPlacePresetRepository
import club.staircrusher.place.domain.model.search.SearchPlacePreset
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class SearchPlacePresetService(
    private val transactionManager: TransactionManager,
    private val searchPlacePresetRepository: SearchPlacePresetRepository,
) {
    fun list() = transactionManager.doInTransaction(isReadOnly = true) {
        searchPlacePresetRepository.findAll().toList()
    }

    fun createKeywordPreset(description: String, searchText: String) = transactionManager.doInTransaction {
        val preset = SearchPlacePreset.ofKeyword(
            id = EntityIdGenerator.generateRandom(),
            description = description,
            searchText = searchText
        )
        searchPlacePresetRepository.save(preset)
    }

    fun deleteById(id: String) = transactionManager.doInTransaction {
        searchPlacePresetRepository.deleteById(id)
    }
}
