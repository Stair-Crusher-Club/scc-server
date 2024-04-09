package club.staircrusher.accessibility.domain.model

import club.staircrusher.stdlib.clock.SccClock
import java.time.Duration
import java.time.Instant

data class PlaceAccessibility(
    val id: String,
    val placeId: String,
    val floors: List<Int>?,
    val isFirstFloor: Boolean,
    val isStairOnlyOption: Boolean?,
    val stairInfo: StairInfo,
    val stairHeightLevel: StairHeightLevel?,
    val hasSlope: Boolean,
    val entranceDoorTypes: List<EntranceDoorType>?,
    val imageUrls: List<String>,
    val userId: String?,
    val createdAt: Instant,
    val deletedAt: Instant? = null,
) {
    fun isDeletable(uid: String?): Boolean {
        return uid != null && uid == userId && SccClock.instant() < createdAt + deletableDuration
    }

    companion object {
        val deletableDuration = Duration.ofHours(6)
    }
}
