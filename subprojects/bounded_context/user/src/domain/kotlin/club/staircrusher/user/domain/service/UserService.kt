package club.staircrusher.user.domain.service

import club.staircrusher.stdlib.domain.DomainException
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.user.domain.entity.User
import club.staircrusher.user.domain.repository.UserRepository
import java.time.Clock

class UserService(
    private val clock: Clock,
    private val userRepository: UserRepository,
    private val passwordEncryptor: PasswordEncryptor,
) {
    data class CreateUserParams(
        val nickname: String,
        val password: String,
        val instagramId: String?
    )

    fun createUser(params: CreateUserParams): User {
        val normalizedNickname = normalizeAndValidateNickname(params.nickname)
        return userRepository.save(
            User(
                id = EntityIdGenerator.generateRandom(),
                nickname = normalizedNickname,
                encryptedPassword = passwordEncryptor.encrypt(params.password.trim()),
                instagramId = params.instagramId?.trim()?.takeIf { it.isNotEmpty() },
                createdAt = clock.instant(),
            )
        )
    }

    fun updateUserInfo(user: User, nickname: String, instagramId: String?): User {
        user.nickname = normalizeAndValidateNickname(nickname, userId = user.id)
        user.instagramId = instagramId?.trim()?.takeIf { it.isNotEmpty() }
        return userRepository.save(user)
    }

    private fun normalizeAndValidateNickname(nickname: String, userId: String? = null): String {
        val normalizedNickname = nickname.trim()
        if (normalizedNickname.length < 2) {
            throw DomainException("최소 2자 이상의 닉네임을 설정해주세요.")
        }
        if (userRepository.findByNickname(normalizedNickname)?.takeIf { it.id != userId } != null) {
            throw DomainException("${normalizedNickname}은 이미 사용 중인 닉네임입니다.")
        }

        return normalizedNickname
    }
}
