package club.staircrusher.spring_web.mock

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.jpa.InMemoryCrudRepositoryMixin
import club.staircrusher.user.application.port.out.persistence.UserProfileRepository
import club.staircrusher.user.domain.model.UserProfile
import org.springframework.context.annotation.Primary
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

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

    override fun findFirstByUserAccountId(userAccountId: String): UserProfile? {
        return entityById.values.find { it.userAccountId == userAccountId }
    }

    override fun findAllByUserAccountIdIn(userAccountIds: Collection<String>): List<UserProfile> {
        return entityById.values.filter { it.userAccountId in userAccountIds }
    }

    override fun findAll(pageable: Pageable): Page<UserProfile> {
        TODO()
    }
}
