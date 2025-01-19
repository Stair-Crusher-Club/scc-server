package club.staircrusher.spring_web.mock

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.jpa.InMemoryCrudRepositoryMixin
import club.staircrusher.user.application.port.out.persistence.IdentifiedUserRepository
import club.staircrusher.user.domain.model.IdentifiedUser
import org.springframework.context.annotation.Primary

@Component
@Primary
class InMemoryIdentifiedUserRepository : IdentifiedUserRepository, InMemoryCrudRepositoryMixin<IdentifiedUser, String>() {
    override val IdentifiedUser.entityId: String
        get() = this.id

    override fun findFirstByNickname(nickname: String): IdentifiedUser? {
        return entityById.values.find { it.nickname == nickname }
    }

    override fun findFirstByEmail(email: String): IdentifiedUser? {
        return entityById.values.find { it.email == email }
    }
}
