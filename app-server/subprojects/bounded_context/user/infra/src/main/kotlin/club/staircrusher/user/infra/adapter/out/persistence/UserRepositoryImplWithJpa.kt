package club.staircrusher.user.infra.adapter.out.persistence

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.user.application.port.out.persistence.UserRepository
import club.staircrusher.user.domain.model.User
import org.springframework.data.repository.findByIdOrNull

@Component
class UserRepositoryImplWithJpa(
    private val delegatee: JpaUserRepository
) : UserRepository {
    override fun save(entity: User): User {
        return delegatee.save(entity)
    }

    override fun saveAll(entities: Collection<User>) {
        delegatee.saveAll(entities)
    }

    override fun removeAll() {
        delegatee.deleteAll()
    }

    override fun findById(id: String): User {
        return delegatee.findByIdOrNull(id) ?: throw IllegalArgumentException("User of id $id does not exist.")
    }

    override fun findByIdOrNull(id: String): User? {
        return delegatee.findByIdOrNull(id)
    }

    override fun findByNickname(nickname: String): User? {
        return delegatee.findFirstByNickname(nickname = nickname)
    }

    override fun findByEmail(email: String): User? {
        return delegatee.findFirstByEmail(email = email)
    }

    override fun findByIdIn(ids: Collection<String>): List<User> {
        return delegatee.findAllById(ids).toList()
    }

    override fun findAll(): List<User> {
        return delegatee.findAll().toList()
    }
}
