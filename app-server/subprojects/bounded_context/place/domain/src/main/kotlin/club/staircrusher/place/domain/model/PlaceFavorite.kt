package club.staircrusher.place.domain.model

import java.time.Instant

class PlaceFavorite(
    val id: String,
    val userId: String,
    val placeId: String,
    var createdAt: Instant,
    var updatedAt: Instant,
    var deletedAt: Instant? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlaceFavorite

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "PlaceFavorite(id='$id', userId='$userId', placeId=$placeId, createdAt=$createdAt, updatedAt=$updatedAt, deletedAt=$deletedAt)"
    }
}
