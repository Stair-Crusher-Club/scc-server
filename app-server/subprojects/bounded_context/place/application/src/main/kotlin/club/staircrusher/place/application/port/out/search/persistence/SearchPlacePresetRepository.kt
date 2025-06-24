package club.staircrusher.place.application.port.out.search.persistence

import club.staircrusher.place.domain.model.search.SearchPlacePreset
import org.springframework.data.repository.CrudRepository

interface SearchPlacePresetRepository : CrudRepository<SearchPlacePreset, String>
