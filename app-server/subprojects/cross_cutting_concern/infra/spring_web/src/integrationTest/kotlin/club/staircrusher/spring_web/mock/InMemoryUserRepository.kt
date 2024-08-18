package club.staircrusher.spring_web.mock

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.jpa.InMemoryCrudRepositoryMixin
import club.staircrusher.user.application.port.out.persistence.UserRepository
import club.staircrusher.user.domain.model.User
import org.springframework.context.annotation.Primary

@Component
@Primary
class InMemoryUserRepository : UserRepository, InMemoryCrudRepositoryMixin<User, String>() {
    override val User.entityId: String
        get() = this.id

    override fun findFirstByNickname(nickname: String): User? {
        return entityById.values.find { it.nickname == nickname }
    }

    override fun findFirstByEmail(email: String): User? {
        return entityById.values.find { it.email == email }
    }
}
