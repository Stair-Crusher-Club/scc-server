package club.staircrusher.user.infra.adapter.out.persistence

import club.staircrusher.user.domain.model.User
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaUserRepository : CrudRepository<User, String> {
    fun findFirstByNickname(nickname: String): User?
    fun findFirstByEmail(email: String): User?
}
