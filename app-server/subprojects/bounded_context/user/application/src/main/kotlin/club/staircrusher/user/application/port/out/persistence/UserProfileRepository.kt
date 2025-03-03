package club.staircrusher.user.application.port.out.persistence

import club.staircrusher.user.domain.model.UserProfile
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserProfileRepository : CrudRepository<UserProfile, String> {
    fun findFirstByUserId(userId: String): UserProfile?
    fun findFirstByNickname(nickname: String): UserProfile?
    fun findFirstByEmail(email: String): UserProfile?
    fun findAllByUserIdIn(userIds: Collection<String>): List<UserProfile>

    data class CreateUserParams(
        val nickname: String,
        @Deprecated("패스워드 로그인은 사라질 예정") val password: String?,
        val instagramId: String?,
        val email: String?,
        val birthYear: Int?,
    )
}
