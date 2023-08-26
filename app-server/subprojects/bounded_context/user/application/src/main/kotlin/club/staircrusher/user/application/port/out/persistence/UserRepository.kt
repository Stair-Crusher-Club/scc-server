package club.staircrusher.user.application.port.out.persistence

import club.staircrusher.stdlib.domain.repository.EntityRepository
import club.staircrusher.user.domain.model.User

interface UserRepository : EntityRepository<User, String> {
    fun findByNickname(nickname: String): User?
    fun findByEmail(email: String): User?
    fun findByIdIn(ids: Collection<String>): List<User>
    fun findAll(): List<User>
    data class CreateUserParams(
        val nickname: String,
        @Deprecated("패스워드 로그인은 사라질 예정") val password: String?,
        val instagramId: String?,
        val email: String?,
    )
}
