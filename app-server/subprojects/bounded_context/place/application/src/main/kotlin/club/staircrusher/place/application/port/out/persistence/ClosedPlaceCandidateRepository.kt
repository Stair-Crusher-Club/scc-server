package club.staircrusher.place.application.port.out.persistence

import club.staircrusher.place.domain.model.ClosedPlaceCandidate
import org.springframework.data.repository.CrudRepository

interface ClosedPlaceCandidateRepository : CrudRepository<ClosedPlaceCandidate, String> {
    fun findByExternalIdIn(externalIds: List<String>): List<ClosedPlaceCandidate>
}
