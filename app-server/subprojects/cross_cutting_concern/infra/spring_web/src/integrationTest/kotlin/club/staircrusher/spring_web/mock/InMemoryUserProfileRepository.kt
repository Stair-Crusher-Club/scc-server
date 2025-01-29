package club.staircrusher.spring_web.mock

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.jpa.InMemoryCrudRepositoryMixin
import club.staircrusher.user.application.port.out.persistence.UserProfileRepository
import club.staircrusher.user.domain.model.UserProfile
import org.springframework.context.annotation.Primary

@Component
@Primary
class InMemoryUserProfileRepository : UserProfileRepository, InMemoryCrudRepositoryMixin<UserProfile, String>() {
    override val UserProfile.entityId: String
        get() = this.id

    override fun findFirstByNickname(nickname: String): UserProfile? {
        return entityById.values.find { it.nickname == nickname }
    }

    override fun findFirstByEmail(email: String): UserProfile? {
        return entityById.values.find { it.email == email }
    }
}
