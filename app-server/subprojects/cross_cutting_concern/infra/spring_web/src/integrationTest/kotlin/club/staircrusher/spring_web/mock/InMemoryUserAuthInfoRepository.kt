package club.staircrusher.spring_web.mock

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.user.application.port.out.persistence.UserAuthInfoRepository
import club.staircrusher.user.domain.model.UserAuthInfo
import club.staircrusher.user.domain.model.UserAuthProviderType
import org.springframework.context.annotation.Primary

@Component
@Primary
class InMemoryUserAuthInfoRepository : UserAuthInfoRepository {
    private val userById = mutableMapOf<String, UserAuthInfo>()
    override fun save(entity: UserAuthInfo): UserAuthInfo {
        userById[entity.id] = entity
        return entity
    }

    override fun saveAll(entities: Collection<UserAuthInfo>) {
        entities.forEach(::save)
    }

    override fun removeAll() {
        userById.clear()
    }

    override fun findById(id: String): UserAuthInfo {
        return userById[id] ?: throw IllegalArgumentException("UserAuthInfo of id $id does not exist.")
    }

    override fun findByIdOrNull(id: String): UserAuthInfo? {
        return userById[id]
    }

    override fun findByExternalId(authProviderType: UserAuthProviderType, externalId: String): UserAuthInfo? {
        return userById.values.find { it.authProviderType == authProviderType && it.externalId == externalId }
    }

    override fun findByUserId(userId: String): List<UserAuthInfo> {
        return userById.values.filter { it.userId == userId }
    }

    override fun removeByUserId(userId: String) {
        userById.values.filter { it.userId == userId }.forEach {
            userById.remove(it.id)
        }
    }
}
