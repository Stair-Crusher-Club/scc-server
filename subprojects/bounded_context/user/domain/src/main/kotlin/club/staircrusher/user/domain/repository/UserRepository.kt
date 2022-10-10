package club.staircrusher.user.domain.repository

import club.staircrusher.stdlib.domain.repository.EntityRepository
import club.staircrusher.user.domain.entity.User

interface UserRepository : EntityRepository<User, String> {
    fun findByNickname(nickname: String): User?
}
