package club.staircrusher.user.infra.adapter.out.persistence

import club.staircrusher.user.domain.entity.User
import club.staircrusher.user.domain.repository.UserRepository
import org.springframework.stereotype.Component

@Component
class InMemoryUserRepository : UserRepository {
    private val userById = mutableMapOf<String, User>()
    override fun findByNickname(nickname: String): User? {
        return userById.values.find { it.nickname == nickname }
    }

    override fun findByIdIn(ids: List<String>): List<User> {
        return ids.mapNotNull { findByIdOrNull(it) }
    }

    override fun save(entity: User): User {
        userById[entity.id] = entity
        return entity
    }

    override fun saveAll(entity: Collection<User>): User {
        entity.forEach { save(it) }
        return entity.first()
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
