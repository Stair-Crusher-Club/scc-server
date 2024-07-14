package club.staircrusher.user.infra.adapter.out.persistence

import club.staircrusher.user.domain.model.UserAuthInfo
import club.staircrusher.user.domain.model.UserAuthProviderType
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaUserAuthInfoRepository : CrudRepository<UserAuthInfo, String> {
    fun findFirstByAuthProviderTypeAndExternalId(authProviderType: UserAuthProviderType, externalId: String): UserAuthInfo?
    fun findByUserId(userId: String): List<UserAuthInfo>
    fun removeByUserId(userId: String)
}
