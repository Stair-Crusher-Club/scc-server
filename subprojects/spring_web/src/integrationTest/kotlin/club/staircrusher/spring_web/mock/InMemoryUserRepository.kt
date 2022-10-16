package club.staircrusher.spring_web.mock

import club.staircrusher.user.domain.model.User
import club.staircrusher.user.application.port.out.persistence.UserRepository
import club.staircrusher.stdlib.di.annotation.Component
import org.junit.jupiter.api.Order
import org.springframework.core.Ordered

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class InMemoryUserRepository : UserRepository {
    private val userById = mutableMapOf<String, User>()
    override fun findByNickname(nickname: String): User? {
        return userById.values.find { it.nickname == nickname }
    }

    override fun findByIdIn(ids: Collection<String>): List<User> {
        return userById.values.filter { it.id in ids }
    }

    override fun save(entity: User): User {
        userById[entity.id] = entity
        return entity
    }

    override fun saveAll(entities: Collection<User>) {
        entities.forEach(::save)
    }

    override fun removeAll() {
        userById.clear()
    }

    override fun findById(id: String): User {
        return userById[id] ?: throw IllegalArgumentException("User of id $id does not exist.")
    }

    override fun findByIdOrNull(id: String): User? {
        return userById[id]
    }
}
