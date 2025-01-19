package club.staircrusher.user.application.port.out.persistence

import club.staircrusher.user.domain.model.IdentifiedUser
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface IdentifiedUserRepository : CrudRepository<IdentifiedUser, String> {
    fun findFirstByNickname(nickname: String): IdentifiedUser?
    fun findFirstByEmail(email: String): IdentifiedUser?

    data class CreateUserParams(
        val nickname: String,
        @Deprecated("패스워드 로그인은 사라질 예정") val password: String?,
        val instagramId: String?,
        val email: String?,
    )
}
