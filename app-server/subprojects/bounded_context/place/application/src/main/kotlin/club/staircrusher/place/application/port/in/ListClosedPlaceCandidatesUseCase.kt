package club.staircrusher.place.application.port.`in`

import club.staircrusher.place.application.port.out.persistence.ClosedPlaceCandidateRepository
import club.staircrusher.place.application.port.out.persistence.PlaceRepository
import club.staircrusher.place.application.result.NamedClosedPlaceCandidate
import club.staircrusher.place.domain.model.ClosedPlaceCandidate
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TimestampCursor
import club.staircrusher.stdlib.persistence.TransactionManager
import org.springframework.data.domain.PageRequest
import java.time.Instant

@Component
class ListClosedPlaceCandidatesUseCase(
    private val transactionManager: TransactionManager,
    private val closedPlaceCandidateRepository: ClosedPlaceCandidateRepository,
    private val placeRepository: PlaceRepository,
) {
    fun handle(
        limit: Int?,
        cursorValue: String?,
    ) = transactionManager.doInTransaction {
        val cursor = cursorValue?.let { Cursor.parse(it) } ?: Cursor.initial()
        val normalizedLimit = limit ?: DEFAULT_LIMIT

        val pageRequest = PageRequest.of(
            0,
            normalizedLimit,
        )
        val result = closedPlaceCandidateRepository.findCursored(
            cursorCreatedAt = cursor.timestamp,
            cursorId = cursor.id,
            pageable = pageRequest,
        )

        val nextCursor = if (result.hasNext()) {
            Cursor(result.content[normalizedLimit - 1])
        } else {
            null
        }

        val placeIds = result.content.map { it.placeId }
        val places = placeRepository.findAllByIdIn(placeIds)
        return@doInTransaction ListClosedPlaceCandidatesResult(
            candidates = result.mapNotNull { candidate ->
                val place = places.find { it.id == candidate.placeId } ?: return@mapNotNull null
                NamedClosedPlaceCandidate(
                    candidateId = candidate.id,
                    placeId = place.id,
                    name = place.name,
                    address = place.address.toString(),
                    acceptedAt = candidate.acceptedAt,
                    ignoredAt = candidate.ignoredAt,
                )
            },
            nextCursor = nextCursor?.value,
        )
    }

    data class ListClosedPlaceCandidatesResult(
        val candidates: List<NamedClosedPlaceCandidate>,
        val nextCursor: String?,
    )

    private data class Cursor(
        val createdAt: Instant,
        val candidateId: String,
    ) : TimestampCursor(createdAt, candidateId) {
        constructor(candidate: ClosedPlaceCandidate) : this(
            createdAt = candidate.createdAt,
            candidateId = candidate.id,
        )

        companion object {
            fun parse(cursorValue: String) = TimestampCursor.parse(cursorValue)

            fun initial() = TimestampCursor.initial()
        }
    }

    companion object {
        private const val DEFAULT_LIMIT = 50
    }
}
