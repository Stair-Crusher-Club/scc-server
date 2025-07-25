package club.staircrusher.place.domain.model.place

import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.persistence.jpa.TimeAuditingBaseEntity
import club.staircrusher.stdlib.place.PlaceCategory
import jakarta.persistence.AttributeOverride
import jakarta.persistence.AttributeOverrides
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Point

@Entity
class Place private constructor(
    @Id
    val id: String,
    val name: String,
    @AttributeOverrides(
        AttributeOverride(name = "lng", column = Column(name = "location_x")),
        AttributeOverride(name = "lat", column = Column(name = "location_y")),
    )
    val location: Location,
    /**
     * geospatial query를 위해 location을 역정규화한 컬럼.
     */
    @Column(name = "location_for_query", columnDefinition = "geography")
    val locationForQuery: Point?,
    @ManyToOne(fetch = FetchType.EAGER)
    val building: Building,
    val siGunGuId: String?,
    val eupMyeonDongId: String?,
    @Enumerated(EnumType.STRING)
    val category: PlaceCategory? = null,
    isClosed: Boolean,
    isNotAccessible: Boolean,
) : TimeAuditingBaseEntity() {
    val address: BuildingAddress
        // FIXME
        get() = building.address

    var isClosed: Boolean = isClosed
        protected set
    var isNotAccessible: Boolean = isNotAccessible
        protected set

    fun setIsClosed(value: Boolean) {
        isClosed = value
    }

    fun setIsNotAccessible(value: Boolean) {
        isNotAccessible = value
    }

    fun isUpdated(maybeUpdated: Place): Boolean {
        check(this.id == maybeUpdated.id)
        return this.locationForQuery == null && maybeUpdated.locationForQuery != null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Place

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "Place(id='$id', name='$name', location=$location, building=$building, siGunGuId=$siGunGuId, " +
            "eupMyeonDongId=$eupMyeonDongId, category=$category, isClosed=$isClosed, isNotAccessible=$isNotAccessible)"
    }

    companion object {
        private val geometryFactory = GeometryFactory()
        fun of(
            id: String,
            name: String,
            location: Location,
            building: Building,
            siGunGuId: String?,
            eupMyeonDongId: String?,
            category: PlaceCategory? = null,
            isClosed: Boolean,
            isNotAccessible: Boolean,
        ): Place {
            return Place(
                id = id,
                name = name,
                location = location,
                locationForQuery = geometryFactory.createPoint(Coordinate(location.lng, location.lat)),
                building = building,
                siGunGuId = siGunGuId,
                eupMyeonDongId = eupMyeonDongId,
                category = category,
                isClosed = isClosed,
                isNotAccessible = isNotAccessible,
            )
        }
    }
}
