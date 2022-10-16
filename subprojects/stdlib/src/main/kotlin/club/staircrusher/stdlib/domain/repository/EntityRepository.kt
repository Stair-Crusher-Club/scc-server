package club.staircrusher.stdlib.domain.repository

interface EntityRepository<ENTITY, ID> {
    fun save(entity: ENTITY): ENTITY
    fun saveAll(entities: Collection<ENTITY>)
    fun removeAll()
    fun findById(id: ID): ENTITY
    fun findByIdOrNull(id: ID): ENTITY?
}
