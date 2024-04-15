package club.staircrusher.place.domain.model

import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.util.Hashing

data class Building(
    val id: String,
    val name: String?,
    val location: Location,
    val address: BuildingAddress,
    val siGunGuId: String,
    val eupMyeonDongId: String,
) {
    companion object {
        fun generateId(roadAddress: String) = Hashing.getHash(
            roadAddress,
            length = 36,
        )
    }

    override fun equals(other: Any?): Boolean {
        return other is Building && other.id == this.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
