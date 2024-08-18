package club.staircrusher.stdlib.persistence.jpa

import org.springframework.data.repository.CrudRepository
import java.util.Optional

abstract class InMemoryCrudRepositoryMixin<ENTITY : Any, ID : Any> : CrudRepository<ENTITY, ID> {
    protected val entityById = mutableMapOf<ID, ENTITY>()
    protected abstract val ENTITY.entityId: ID

    override fun findById(id: ID): Optional<ENTITY> {
        return Optional.ofNullable(entityById[id])
    }

    override fun existsById(id: ID): Boolean {
        return id in entityById
    }

    override fun findAll(): Iterable<ENTITY> {
        return entityById.values
    }

    override fun findAllById(ids: Iterable<ID>): Iterable<ENTITY> {
        return entityById.values.filter { it.entityId in ids }
    }

    override fun count(): Long {
        return entityById.count().toLong()
    }

    override fun deleteById(id: ID) {
        entityById.remove(id)
    }

    override fun delete(entity: ENTITY) {
        deleteById(entity.entityId)
    }

    override fun deleteAllById(ids: Iterable<ID>) {
        ids.forEach(::deleteById)
    }

    override fun deleteAll(entities: Iterable<ENTITY>) {
        entities.forEach(::delete)
    }

    override fun deleteAll() {
        entityById.clear()
    }

    override fun <S : ENTITY> save(entity: S): S {
        entityById[entity.entityId] = entity
        return entity
    }

    override fun <S : ENTITY> saveAll(entities: Iterable<S>): Iterable<S> {
        entities.forEach(::save)
        return entities
    }
}
