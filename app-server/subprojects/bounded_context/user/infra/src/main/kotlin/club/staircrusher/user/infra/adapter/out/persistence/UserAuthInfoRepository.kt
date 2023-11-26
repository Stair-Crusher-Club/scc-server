package club.staircrusher.user.infra.adapter.out.persistence

import club.staircrusher.infra.persistence.sqldelight.DB
import club.staircrusher.user.domain.model.UserAuthInfo
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.user.domain.model.UserAuthProviderType

@Component
class UserAuthInfoRepository(
    db: DB,
) : club.staircrusher.user.application.port.out.persistence.UserAuthInfoRepository {
    private val queries = db.userAuthInfoQueries
    override fun save(entity: UserAuthInfo): UserAuthInfo {
        queries.save(entity.toPersistenceModel())
        return entity
    }

    override fun saveAll(entities: Collection<UserAuthInfo>) {
        entities.forEach(::save)
    }

    override fun removeAll() {
        queries.removeAll()
    }

    override fun findById(id: String): UserAuthInfo {
        return findByIdOrNull(id) ?: throw IllegalArgumentException("UserAuthInfo of id $id does not exist.")
    }

    override fun findByIdOrNull(id: String): UserAuthInfo? {
        return queries.findById(id = id)
            .executeAsOneOrNull()
            ?.toDomainModel()
    }

    override fun findByExternalId(authProviderType: UserAuthProviderType, externalId: String): UserAuthInfo? {
        return queries.findByExternalId(authProviderType, externalId)
            .executeAsOneOrNull()
            ?.toDomainModel()
    }

    override fun findByUserId(userId: String): List<UserAuthInfo> {
        return queries.findByUserId(userId)
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun removeByUserId(userId: String) {
        queries.removeByUserId(userId)
    }
}
