package club.staircrusher.spring_web.mock

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.InMemoryCrudRepositoryMixin
import club.staircrusher.user.application.port.out.persistence.UserAuthInfoRepository
import club.staircrusher.user.domain.model.UserAuthInfo
import club.staircrusher.user.domain.model.UserAuthProviderType
import org.springframework.context.annotation.Primary

@Component
@Primary
class InMemoryUserAuthInfoRepository : UserAuthInfoRepository, InMemoryCrudRepositoryMixin<UserAuthInfo, String>() {
    override val UserAuthInfo.entityId: String
        get() = this.id

    override fun findFirstByAuthProviderTypeAndExternalId(authProviderType: UserAuthProviderType, externalId: String): UserAuthInfo? {
        return entityById.values.find { it.authProviderType == authProviderType && it.externalId == externalId }
    }

    override fun findByUserId(userId: String): List<UserAuthInfo> {
        return entityById.values.filter { it.userId == userId }
    }

    override fun removeByUserId(userId: String) {
        entityById.values.filter { it.userId == userId }.forEach {
            entityById.remove(it.id)
        }
    }
}
