package club.staircrusher.place.domain.model

import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.util.Hashing
import jakarta.persistence.AttributeOverride
import jakarta.persistence.AttributeOverrides
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class Building(
    @Id
    val id: String,
    val name: String?,
    @AttributeOverrides(
        AttributeOverride(name = "lng", column = Column(name = "location_x")),
        AttributeOverride(name = "lat", column = Column(name = "location_y")),
    )
    val location: Location,
    @AttributeOverrides(
        AttributeOverride(name = "siDo", column = Column(name = "address_si_do")),
        AttributeOverride(name = "siGunGu", column = Column(name = "address_si_gun_gu")),
        AttributeOverride(name = "eupMyeonDong", column = Column(name = "address_eup_myeon_dong")),
        AttributeOverride(name = "li", column = Column(name = "address_li")),
        AttributeOverride(name = "roadName", column = Column(name = "address_road_name")),
        AttributeOverride(name = "mainBuildingNumber", column = Column(name = "address_main_building_number")),
        AttributeOverride(name = "subBuildingNumber", column = Column(name = "address_sub_building_number")),
    )
    val address: BuildingAddress,
    val siGunGuId: String,
    val eupMyeonDongId: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Building

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "Building(id='$id', name=$name, location=$location, address=$address, siGunGuId='$siGunGuId', " +
            "eupMyeonDongId='$eupMyeonDongId')"
    }

    companion object {
        fun generateId(roadAddress: String) = Hashing.getHash(
            roadAddress,
            length = 36,
        )
    }
}
