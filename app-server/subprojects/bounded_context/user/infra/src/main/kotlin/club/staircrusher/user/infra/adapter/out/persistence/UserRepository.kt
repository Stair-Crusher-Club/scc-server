package club.staircrusher.user.infra.adapter.out.persistence

import club.staircrusher.infra.persistence.sqldelight.DB
import club.staircrusher.user.domain.model.User
import club.staircrusher.user.application.port.out.persistence.UserRepository
import club.staircrusher.stdlib.di.annotation.Component

@Component
class UserRepository(
    db: DB,
) : UserRepository {
    private val queries = db.userQueries

    override fun save(entity: User): User {
        queries.save(entity.toPersistenceModel())
        return entity
    }

    override fun saveAll(entities: Collection<User>) {
        entities.forEach(::save)
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
        if (ids.isEmpty()) {
            // empty list로 쿼리를 할 경우 sqldelight가 제대로 처리하지 못하는 문제가 있다.
            // select * from entity where entity.id in (); <- 이런 식으로 쿼리를 날리는데, () 부분이 syntax error이다.
            // 따라서 ids가 empty면 early return을 해준다.
            return emptyList()
        }
        return queries.findByIdIn(ids = ids)
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun findAll(): List<User> {
        return queries.findAll().executeAsList().map { it.toDomainModel() }
    }
}
