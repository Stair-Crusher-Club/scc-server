package club.staircrusher.user.application.port.out.persistence

import club.staircrusher.stdlib.domain.repository.EntityRepository
import club.staircrusher.user.domain.model.User

interface UserRepository : EntityRepository<User, String> {
    fun findByNickname(nickname: String): User?
    fun findByIdIn(ids: Collection<String>): List<User>
    fun findAll(): List<User>
    data class CreateUserParams(
        val nickname: String,
        val password: String,
        val instagramId: String?
    )
}
