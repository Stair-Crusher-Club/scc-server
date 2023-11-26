package club.staircrusher.user.application.port.out.persistence

import club.staircrusher.stdlib.domain.repository.EntityRepository
import club.staircrusher.user.domain.model.UserAuthInfo
import club.staircrusher.user.domain.model.UserAuthProviderType

interface UserAuthInfoRepository : EntityRepository<UserAuthInfo, String> {
    fun findByExternalId(authProviderType: UserAuthProviderType, externalId: String): UserAuthInfo?
    fun findByUserId(userId: String): List<UserAuthInfo>
    fun removeByUserId(userId: String)
}
