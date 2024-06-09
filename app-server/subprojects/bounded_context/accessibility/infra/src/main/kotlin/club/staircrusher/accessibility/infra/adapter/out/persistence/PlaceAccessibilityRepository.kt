package club.staircrusher.accessibility.infra.adapter.out.persistence

import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.accessibility.domain.model.AccessibilityImage
import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.accessibility.infra.adapter.out.persistence.sqldelight.toDomainModel
import club.staircrusher.accessibility.infra.adapter.out.persistence.sqldelight.toPersistenceModel
import club.staircrusher.infra.persistence.sqldelight.DB
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.geography.EupMyeonDong
import club.staircrusher.stdlib.time.toOffsetDateTime
import java.time.Instant

@Suppress("TooManyFunctions")
@Component
class PlaceAccessibilityRepository(
    db: DB,
) : PlaceAccessibilityRepository {
    private val queries = db.placeAccessibilityQueries

    override fun save(entity: PlaceAccessibility): PlaceAccessibility {
        queries.save(entity.toPersistenceModel().copy(updated_at = SccClock.instant().toOffsetDateTime()))
        return entity
    }

    override fun saveAll(entities: Collection<PlaceAccessibility>) {
        entities.forEach(::save)
    }

    override fun removeAll() {
        queries.removeAll()
    }

    override fun findById(id: String): PlaceAccessibility {
        return findByIdOrNull(id) ?: throw IllegalArgumentException("PlaceAccessibility of id $id does not exist.")
    }

    override fun findByIdOrNull(id: String): PlaceAccessibility? {
        return queries.findById(id = id).executeAsOneOrNull()?.toDomainModel()
    }

    override fun findByPlaceIds(placeIds: Collection<String>): List<PlaceAccessibility> {
        return queries.findByPlaceIds(placeIds = placeIds)
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun findByPlaceId(placeId: String): PlaceAccessibility? {
        return queries.findByPlaceId(placeId = placeId)
            .executeAsOneOrNull()
            ?.toDomainModel()
    }

    override fun findByUserId(userId: String): List<PlaceAccessibility> {
        return queries.findByUserId(userId = userId)
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun findByCreatedAtGreaterThanAndOrderByCreatedAtAsc(createdAt: Instant?): List<PlaceAccessibility> {
        return queries.findByCreatedAtGreaterThanAndOrderByCreatedAtAsc(
            createdAt = (createdAt ?: SccClock.instant()).toOffsetDateTime(), limit = 1
        )
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun countByEupMyeonDong(eupMyeonDong: EupMyeonDong): Int {
        return queries.countByEupMyeonDong(eupMyeonDongId = eupMyeonDong.id)
            .executeAsOne()
            .toInt()
    }

    override fun countByUserId(userId: String): Int {
        return queries.countByUserId(userId = userId).executeAsOne().toInt()
    }

    override fun hasAccessibilityNotRegisteredPlaceInBuilding(buildingId: String): Boolean {
        return queries.hasAccessibilityNotRegisteredPlaceInBuilding(buildingId = buildingId)
            .executeAsOne()
    }

    override fun findByBuildingId(buildingId: String): List<PlaceAccessibility> {
        return queries.findByBuildingId(buildingId = buildingId)
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun searchForAdmin(
        placeName: String?,
        createdAtFrom: Instant?,
        createdAtToExclusive: Instant?,
        cursorCreatedAt: Instant,
        cursorId: String,
        limit: Int
    ): List<PlaceAccessibility> {
        return queries.searchForAdmin(
            placeNameLike = placeName?.let { "%$it%" },
            createdAtFrom = (createdAtFrom ?: Instant.EPOCH).toOffsetDateTime(),
            createdAtToExclusive = (createdAtToExclusive ?: SccClock.instant()).toOffsetDateTime(),
            cursorCreatedAt = cursorCreatedAt.toOffsetDateTime(),
            cursorId = cursorId,
            limit = limit.toLong(),
        )
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun updateImages(id: String, images: List<AccessibilityImage>) {
        return queries.updateImages(images, id)
    }

    override fun countAll(): Int {
        return queries.countAll()
            .executeAsOne()
            .toInt()
    }

    override fun remove(id: String) {
        queries.remove(SccClock.instant().toOffsetDateTime(), id)
    }
}
