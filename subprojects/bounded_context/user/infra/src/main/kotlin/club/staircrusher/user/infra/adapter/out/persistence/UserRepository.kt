package club.staircrusher.user.infra.adapter.out.persistence

import club.staircrusher.infra.persistence.sqldelight.DB
import club.staircrusher.user.domain.entity.User
import club.staircrusher.user.domain.repository.UserRepository
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.user.infra.adapter.out.persistence.sqldelight.toDomainModel
import club.staircrusher.user.infra.adapter.out.persistence.sqldelight.toPersistenceModel

@Component
class UserRepository(
    db: DB,
) : UserRepository {
    private val queries = db.userQueries

    override fun save(entity: User): User {
        queries.save(entity.toPersistenceModel())
        return entity
    }

    override fun saveAll(entity: Collection<User>): User {
        entity.forEach(::save)
        return entity.first()
    }

    override fun removeAll() {
        queries.removeAll()
    }

    override fun findById(id: String): User {
        return findByIdOrNull(id) ?: throw IllegalArgumentException("User of id $id does not exist.")
    }

    override fun findByIdOrNull(id: String): User? {
        return queries.findById(id = id)
            .executeAsOneOrNull()
            ?.toDomainModel()
    }

    override fun findByNickname(nickname: String): User? {
        return queries.findByNickname(nickname = nickname)
            .executeAsOneOrNull()
            ?.toDomainModel()
    }

    override fun findByIdIn(ids: Collection<String>): List<User> {
        return queries.findByIdIn(ids = ids)
            .executeAsList()
            .map { it.toDomainModel() }
    }
}
