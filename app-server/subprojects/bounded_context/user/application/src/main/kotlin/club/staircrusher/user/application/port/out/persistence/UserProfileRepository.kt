package club.staircrusher.user.application.port.out.persistence

import club.staircrusher.user.domain.model.UserProfile
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserProfileRepository : CrudRepository<UserProfile, String> {
    fun findFirstByUserAccountId(userAccountId: String): UserProfile?
    fun findFirstByNickname(nickname: String): UserProfile?
    fun findFirstByEmail(email: String): UserProfile?
    fun findAllByUserAccountIdIn(userAccountIds: Collection<String>): List<UserProfile>
    fun findAll(pageable: Pageable): Page<UserProfile>

    data class CreateUserParams(
        val nickname: String,
        @Deprecated("패스워드 로그인은 사라질 예정") val password: String?,
        val instagramId: String?,
        val email: String?,
    )
}
