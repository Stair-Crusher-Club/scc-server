package club.staircrusher.place.application.result

import java.time.Instant

data class NamedClosedPlaceCandidate(
    val candidateId: String,
    val placeId: String,
    val name: String,
    val address: String,
    val acceptedAt: Instant?,
    val ignoredAt: Instant?,
)
