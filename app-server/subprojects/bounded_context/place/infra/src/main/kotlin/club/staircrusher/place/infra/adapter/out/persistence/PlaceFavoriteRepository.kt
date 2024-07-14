package club.staircrusher.place.infra.adapter.out.persistence

import club.staircrusher.infra.persistence.sqldelight.DB
import club.staircrusher.place.application.port.out.persistence.PlaceFavoriteRepository
import club.staircrusher.place.domain.model.PlaceFavorite
import club.staircrusher.stdlib.di.annotation.Component

@Component
class PlaceFavoriteRepository(db: DB) : PlaceFavoriteRepository {
    private val placeFavoriteQueries = db.placeFavoriteQueries

    override fun findByUserId(userId: String): List<PlaceFavorite> {
        return placeFavoriteQueries.findPlaceFavoriteByUserId(userId)
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun findByUserIdAndPlaceId(userId: String, placeId: String): PlaceFavorite? {
        return placeFavoriteQueries.findPlaceFavoriteByUserIdAndPlaceId(userId, placeId)
            .executeAsOneOrNull()
            ?.toDomainModel()
    }

    override fun findByPlaceID(placeId: String): List<PlaceFavorite> {
        return placeFavoriteQueries.findPlaceFavoriteByPlaceIdAndDeletedAtIsNull(placeId)
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun countByPlaceId(placeId: String): Long {
        return placeFavoriteQueries.countPlaceFavoritesByPlaceIdAndDeletedAtIsNull(placeId)
            .executeAsOne()
    }

    override fun save(entity: PlaceFavorite): PlaceFavorite {
        placeFavoriteQueries.save(entity.toPersistenceModel())
        return entity
    }

    override fun saveAll(entities: Collection<PlaceFavorite>) {
        entities.forEach(::save)
    }

    override fun removeAll() {
        placeFavoriteQueries.removeAll()
    }

    override fun findById(id: String): PlaceFavorite {
        return findByIdOrNull(id) ?: throw IllegalArgumentException("PlaceFavorite of id $id does not exist.")
    }

    override fun findByIdOrNull(id: String): PlaceFavorite? {
        return placeFavoriteQueries.findPlaceFavoriteById(id).executeAsOneOrNull()?.toDomainModel()
    }
}
