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

    override fun findFirstByUserId(userId: String): UserProfile? {
        return entityById.values.find { it.userId == userId }
    }

    override fun findAllByUserIdIn(userIds: Collection<String>): List<UserProfile> {
        return entityById.values.filter { it.userId in userIds }
    }

    override fun existsByNickname(nickname: String): Boolean {
        return entityById.values.find { it.nickname == nickname } != null
    }

    override fun existsByEmail(email: String): Boolean {
        return entityById.values.find { it.email == email } != null
    }
}
