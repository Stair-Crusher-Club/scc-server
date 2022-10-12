package club.staircrusher.user.domain.service

import club.staircrusher.stdlib.domain.DomainException
import club.staircrusher.user.domain.entity.User
import club.staircrusher.user.domain.repository.UserRepository
import club.staircrusher.user.domain.exception.UserAuthenticationException
import club.staircrusher.stdlib.di.annotation.Component

@Component
class UserAuthService(
    private val tokenManager: TokenManager,
    private val userRepository: UserRepository,
    private val passwordEncryptor: PasswordEncryptor,
) {
    fun authenticate(nickname: String, password: String): User {
        val user = userRepository.findByNickname(nickname) ?: throw DomainException("잘못된 계정 아이디입니다.")
        if (!passwordEncryptor.verify(password, user.encryptedPassword)) {
            throw DomainException("잘못된 비밀번호입니다.")
        }
        return user
    }

    fun issueAccessToken(user: User): String {
        return tokenManager.issueToken(UserAccessTokenPayload(
            userId = user.id,
        ))
    }

    @Throws(TokenVerificationException::class)
    fun verifyAccessToken(token: String): UserAccessTokenPayload {
        return try {
            tokenManager.verify(token, UserAccessTokenPayload::class)
        } catch (_: TokenVerificationException) {
            throw UserAuthenticationException()
        }
    }
}
