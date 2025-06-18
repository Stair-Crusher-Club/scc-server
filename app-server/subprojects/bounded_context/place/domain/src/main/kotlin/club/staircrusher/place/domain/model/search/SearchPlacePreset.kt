package club.staircrusher.place.domain.model.search

import club.staircrusher.stdlib.persistence.jpa.TimeAuditingBaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
class SearchPlacePreset(
    @Id
    val id: String,
    @Enumerated(EnumType.STRING)
    val type: PresetType,
    val description: String,
    val searchText: String,
    @JdbcTypeCode(SqlTypes.JSON)
    val filter: SearchPlaceFilter?,
    @Enumerated(EnumType.STRING)
    val sort: SearchPlaceSort?,
) : TimeAuditingBaseEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SearchPlacePreset

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "SearchPlacePreset(id='$id', type='$type', description='$description', searchText='$searchText', " +
            "filter=$filter, sort=$sort, createdAt=$createdAt, updatedAt=$updatedAt)"
    }

    companion object {
        fun ofKeyword(id: String, description: String, searchText: String): SearchPlacePreset {
            return SearchPlacePreset(
                id = id,
                type = PresetType.KEYWORD,
                description = description,
                searchText = searchText,
                filter = null,
                sort = null
            )
        }
    }
}
