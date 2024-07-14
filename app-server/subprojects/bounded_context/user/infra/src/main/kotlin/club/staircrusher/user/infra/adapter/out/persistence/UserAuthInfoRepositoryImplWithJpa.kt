package club.staircrusher.user.infra.adapter.out.persistence

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.user.domain.model.UserAuthInfo
import club.staircrusher.user.domain.model.UserAuthProviderType
import org.springframework.data.repository.findByIdOrNull

@Component
class UserAuthInfoRepositoryImplWithJpa(
    private val delegatee: JpaUserAuthInfoRepository,
) : club.staircrusher.user.application.port.out.persistence.UserAuthInfoRepository {
    override fun save(entity: UserAuthInfo): UserAuthInfo {
        return delegatee.save(entity)
    }

    override fun saveAll(entities: Collection<UserAuthInfo>) {
        delegatee.saveAll(entities)
    }

    override fun removeAll() {
        delegatee.deleteAll()
    }

    override fun findById(id: String): UserAuthInfo {
        return delegatee.findByIdOrNull(id) ?: throw IllegalArgumentException("UserAuthInfo of id $id does not exist.")
    }

    override fun findByIdOrNull(id: String): UserAuthInfo? {
        return delegatee.findByIdOrNull(id)
    }

    override fun findByExternalId(authProviderType: UserAuthProviderType, externalId: String): UserAuthInfo? {
        return delegatee.findFirstByAuthProviderTypeAndExternalId(authProviderType, externalId)
    }

    override fun findByUserId(userId: String): List<UserAuthInfo> {
        return delegatee.findByUserId(userId)
    }

    override fun removeByUserId(userId: String) {
        delegatee.removeByUserId(userId)
    }
}
