package club.staircrusher.place.application.port.out.persistence

import club.staircrusher.place.domain.model.ClosedPlaceCandidate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.Instant

interface ClosedPlaceCandidateRepository : CrudRepository<ClosedPlaceCandidate, String> {
    @Query("""
        SELECT c
        FROM ClosedPlaceCandidate c
        WHERE
            (
                (c.createdAt = :cursorCreatedAt AND c.id < :cursorId)
                OR (c.createdAt < :cursorCreatedAt)
            )
        ORDER BY c.createdAt DESC, c.id DESC
    """)
    fun findCursored(
        cursorCreatedAt: Instant,
        cursorId: String,
        pageable: Pageable,
    ): Page<ClosedPlaceCandidate>

    fun findByExternalIdIn(externalIds: List<String>): List<ClosedPlaceCandidate>
    fun findByPlaceIdIn(placeIds: List<String>): List<ClosedPlaceCandidate>
    fun findByPlaceId(placeId: String): ClosedPlaceCandidate?
}
