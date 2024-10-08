package club.staircrusher.place.application.result

import club.staircrusher.stdlib.geography.Location
import java.time.LocalDate

data class ClosedPlaceResult(
    val externalId: String,
    val name: String,
    val postalCode: String,
    val location: Location,
    val phoneNumber: String?,
    val closedDate: LocalDate,
)
