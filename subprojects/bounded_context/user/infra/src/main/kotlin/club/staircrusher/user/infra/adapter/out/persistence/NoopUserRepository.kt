package club.staircrusher.user.infra.adapter.out.persistence

import club.staircrusher.user.domain.entity.User
import club.staircrusher.user.domain.repository.UserRepository
import org.springframework.stereotype.Component

@Component
class NoOpUserRepository : UserRepository {
    override fun findByNickname(nickname: String): User? {
        return null
    }

    override fun findByIdIn(ids: List<String>): List<User> {
        return emptyList()
    }

    override fun save(entity: User): User {
        return entity
    }

    override fun saveAll(entity: Collection<User>): User {
        return entity.first()
    }

    override fun removeAll() {
        // No-op
    }

    override fun findById(id: String): User {
        throw IllegalArgumentException("User of id $id does not exist.")
    }

    override fun findByIdOrNull(id: String): User? {
        return null
    }
}
