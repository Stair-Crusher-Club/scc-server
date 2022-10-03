package club.staircrusher.user.application.user

import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.domain.entity.User
import club.staircrusher.user.domain.repository.UserRepository
import club.staircrusher.user.domain.service.UserAuthService
import club.staircrusher.user.domain.service.UserService
import org.springframework.stereotype.Component

@Component
class UserApplicationService(
    private val transactionManager: TransactionManager,
    private val userRepository: UserRepository,
    private val userService: UserService,
    private val userAuthService: UserAuthService,
) {
    fun signUp(
        nickname: String,
        password: String,
        instagramId: String?
    ): LoginResult = transactionManager.doInTransaction(TransactionIsolationLevel.SERIALIZABLE) {
        val user = userService.createUser(
            UserService.CreateUserParams(
                nickname = nickname,
                password = password,
                instagramId = instagramId,
            )
        )
        val accessToken = userAuthService.issueAccessToken(user)
        LoginResult(user, accessToken)
    }

    fun login(
        nickname: String,
        password: String
    ): LoginResult = transactionManager.doInTransaction {
        val user = userAuthService.authenticate(nickname, password)
        val accessToken = userAuthService.issueAccessToken(user)
        LoginResult(user, accessToken)
    }

    data class LoginResult(
        val user: User,
        val accessToken: String
    )

    fun updateUserInfo(
        userId: String,
        nickname: String,
        instagramId: String?
    ): User = transactionManager.doInTransaction(TransactionIsolationLevel.SERIALIZABLE) {
        val user = userRepository.findById(userId)
        userService.updateUserInfo(user, nickname, instagramId)
    }
}