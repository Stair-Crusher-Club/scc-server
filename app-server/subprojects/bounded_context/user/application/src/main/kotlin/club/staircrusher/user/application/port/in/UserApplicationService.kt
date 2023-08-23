package club.staircrusher.user.application.port.`in`

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.domain.model.LoginResult
import club.staircrusher.user.application.port.out.persistence.UserRepository
import club.staircrusher.user.domain.model.User
import club.staircrusher.user.domain.service.PasswordEncryptor
import club.staircrusher.user.domain.service.UserAuthService
import java.time.Clock

@Component
class UserApplicationService(
    private val transactionManager: TransactionManager,
    private val userRepository: UserRepository,
    private val userAuthService: UserAuthService,
    private val passwordEncryptor: PasswordEncryptor,
    private val clock: Clock,
) {
    @Deprecated("닉네임 로그인은 사라질 예정")
    fun signUpWithNicknameAndPassword(
        nickname: String,
        password: String,
        instagramId: String?
    ): LoginResult = transactionManager.doInTransaction(TransactionIsolationLevel.REPEATABLE_READ) {
        val params = UserRepository.CreateUserParams(
            nickname = nickname,
            password = password,
            instagramId = instagramId,
            email = null,
        )

        val user = signUp(params)

        val accessToken = userAuthService.issueAccessToken(user)
        LoginResult(accessToken)
    }

    fun signUp(
        params: UserRepository.CreateUserParams
    ): User {
        val normalizedNickname = params.nickname.trim()
        if (normalizedNickname.length < 2) {
            throw SccDomainException("최소 2자 이상의 닉네임을 설정해주세요.")
        }
        if (userRepository.findByNickname(normalizedNickname) != null) {
            throw SccDomainException("${normalizedNickname}은 이미 사용된 닉네임입니다.")
        }
        return userRepository.save(
            User(
                id = EntityIdGenerator.generateRandom(),
                nickname = normalizedNickname,
                encryptedPassword = params.password?.trim()?.let { passwordEncryptor.encrypt(it) },
                instagramId = params.instagramId?.trim()?.takeIf { it.isNotEmpty() },
                email = params.email,
                createdAt = clock.instant(),
            )
        )
    }

    @Deprecated("닉네임 로그인은 사라질 예정")
    fun login(
        nickname: String,
        password: String
    ): LoginResult = transactionManager.doInTransaction {
        val user = userRepository.findByNickname(nickname) ?: throw SccDomainException("잘못된 계정입니다.")
        if (user.isDeleted) {
            throw SccDomainException("잘못된 계정입니다.")
        }
        if (!passwordEncryptor.verify(password, user.encryptedPassword ?: "")) {
            throw SccDomainException("잘못된 비밀번호입니다.")
        }
        val accessToken = userAuthService.issueAccessToken(user)
        LoginResult(accessToken)
    }

    fun updateUserInfo(
        userId: String,
        nickname: String,
        instagramId: String?
    ): User = transactionManager.doInTransaction(TransactionIsolationLevel.REPEATABLE_READ) {
        val user = userRepository.findById(userId)
        user.nickname = run {
            val normalizedNickname = nickname.trim()
            if (normalizedNickname.length < 2) {
                throw SccDomainException("최소 2자 이상의 닉네임을 설정해주세요.")
            }
            if (userRepository.findByNickname(normalizedNickname)?.takeIf { it.id != user.id } != null) {
                throw SccDomainException("${normalizedNickname}은 이미 사용 중인 닉네임입니다.")
            }
            normalizedNickname
        }
        user.instagramId = instagramId?.trim()?.takeIf { it.isNotEmpty() }
        userRepository.save(user)
    }

    fun deleteUser(
        userId: String,
    ) = transactionManager.doInTransaction (TransactionIsolationLevel.REPEATABLE_READ) {
        val user = userRepository.findById(userId)
        user.delete(clock.instant())
        userRepository.save(user)
    }

    fun getUser(userId: String): User? = transactionManager.doInTransaction {
        userRepository.findByIdOrNull(userId)
    }

    fun getUsers(userIds: List<String>): List<User> = transactionManager.doInTransaction {
        userRepository.findByIdIn(userIds)
    }

    fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }
}
