package club.staircrusher.user.application.port.`in`

import club.staircrusher.application.server_event.port.`in`.SccServerEventRecorder
import club.staircrusher.domain.server_event.NewsletterSubscribedOnSignupPayload
import club.staircrusher.notification.port.`in`.PushService
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.stdlib.validation.email.EmailValidator
import club.staircrusher.user.application.port.out.persistence.UserAuthInfoRepository
import club.staircrusher.user.domain.model.AuthTokens
import club.staircrusher.user.application.port.out.persistence.UserRepository
import club.staircrusher.user.application.port.out.web.subscription.StibeeSubscriptionService
import club.staircrusher.user.domain.model.User
import club.staircrusher.user.domain.model.UserMobilityTool
import club.staircrusher.user.domain.service.PasswordEncryptor
import club.staircrusher.user.domain.service.UserAuthService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.data.repository.findByIdOrNull

@Component
class UserApplicationService(
    private val transactionManager: TransactionManager,
    private val userRepository: UserRepository,
    private val userAuthService: UserAuthService,
    private val passwordEncryptor: PasswordEncryptor,
    private val userAuthInfoRepository: UserAuthInfoRepository,
    private val stibeeSubscriptionService: StibeeSubscriptionService,
    private val sccServerEventRecorder: SccServerEventRecorder,
    private val pushService: PushService,
) {
    private val logger = KotlinLogging.logger {}

    @Deprecated("닉네임 로그인은 사라질 예정")
    fun signUpWithNicknameAndPassword(
        nickname: String,
        password: String,
        instagramId: String?
    ): AuthTokens = transactionManager.doInTransaction(TransactionIsolationLevel.REPEATABLE_READ) {
        val params = UserRepository.CreateUserParams(
            nickname = nickname,
            password = password,
            instagramId = instagramId,
            email = null,
        )

        val user = signUp(params)

        val accessToken = userAuthService.issueAccessToken(user)
        AuthTokens(accessToken)
    }

    fun signUp(
        params: UserRepository.CreateUserParams
    ): User {
        val normalizedNickname = params.nickname.trim()
        if (normalizedNickname.length < 2) {
            throw SccDomainException("최소 2자 이상의 닉네임을 설정해주세요.")
        }
        if (userRepository.findFirstByNickname(normalizedNickname) != null) {
            throw SccDomainException("${normalizedNickname}은 이미 사용된 닉네임입니다.")
        }
        return userRepository.save(
            User(
                id = EntityIdGenerator.generateRandom(),
                nickname = normalizedNickname,
                encryptedPassword = params.password?.trim()?.let { passwordEncryptor.encrypt(it) },
                instagramId = params.instagramId?.trim()?.takeIf { it.isNotEmpty() },
                email = params.email,
                mobilityTools = mutableListOf(),
                createdAt = SccClock.instant(),
            )
        )
    }

    @Deprecated("닉네임 로그인은 사라질 예정")
    fun login(
        nickname: String,
        password: String
    ): AuthTokens = transactionManager.doInTransaction {
        val user = userRepository.findFirstByNickname(nickname) ?: throw SccDomainException("잘못된 계정입니다.")
        if (user.isDeleted) {
            throw SccDomainException("잘못된 계정입니다.")
        }
        if (!passwordEncryptor.verify(password, user.encryptedPassword ?: "")) {
            throw SccDomainException("잘못된 비밀번호입니다.")
        }
        val accessToken = userAuthService.issueAccessToken(user)
        AuthTokens(accessToken)
    }

    fun updatePushToken(
        userId: String,
        pushToken: String,
    ): User = transactionManager.doInTransaction {
        val user = userRepository.findById(userId).get()
        user.pushToken = pushToken
        userRepository.save(user)
    }

    fun sendPushNotification(
        userIds: List<String>,
        title: String?,
        body: String,
        deepLink: String?,
    ) = transactionManager.doInTransaction {
        val users = userRepository.findAllById(userIds)
        val notifications = users.mapNotNull { user ->
            user.pushToken ?: return@mapNotNull null
            user.pushToken!! to PushService.Notification(
                // just poc for now, but not sure this substitution needs to be placed here
                title = title?.replace("{{nickname}}", user.nickname),
                body = body.replace("{{nickname}}", user.nickname),
                link = deepLink,
                collapseKey = null,
            )
        }

        transactionManager.doAfterCommit {
            CoroutineScope(Dispatchers.IO).launch {
                notifications.map { (t, n) ->
                    async { pushService.send(t, emptyMap(), n) }
                }.joinAll()
            }
        }
    }

    fun updateUserInfo(
        userId: String,
        nickname: String,
        instagramId: String?,
        email: String,
        mobilityTools: List<UserMobilityTool>,
        isNewsLetterSubscriptionAgreed: Boolean,
    ): User = transactionManager.doInTransaction(TransactionIsolationLevel.REPEATABLE_READ) {
        val user = userRepository.findById(userId).get()
        user.nickname = run {
            val normalizedNickname = nickname.trim()
            if (normalizedNickname.length < 2) {
                throw SccDomainException(
                    "최소 2자 이상의 닉네임을 설정해주세요.",
                    SccDomainException.ErrorCode.INVALID_NICKNAME,
                )
            }
            if (userRepository.findFirstByNickname(normalizedNickname)?.takeIf { it.id != user.id } != null) {
                throw SccDomainException(
                    "${normalizedNickname}은 이미 사용 중인 닉네임입니다.",
                    SccDomainException.ErrorCode.INVALID_NICKNAME,
                )
            }
            normalizedNickname
        }
        user.email = run {
            val normalizedEmail = email.trim()
            if (!EmailValidator.isValid(normalizedEmail)) {
                throw SccDomainException(
                    "${normalizedEmail}은 유효한 형태의 이메일이 아닙니다.",
                    SccDomainException.ErrorCode.INVALID_EMAIL,
                )
            }
            if (userRepository.findFirstByEmail(normalizedEmail)?.takeIf { it.id != user.id } != null) {
                throw SccDomainException(
                    "${normalizedEmail}은 이미 사용 중인 이메일입니다.",
                    SccDomainException.ErrorCode.INVALID_EMAIL,
                )
            }
            normalizedEmail
        }
        user.instagramId = instagramId?.trim()?.takeIf { it.isNotEmpty() }
        user.mobilityTools.clear()
        user.mobilityTools.addAll(mobilityTools)
        userRepository.save(user)

        if (isNewsLetterSubscriptionAgreed) {
            user.email?.let {
                transactionManager.doAfterCommit {
                    subscribeToNewsLetter(user.id, it, user.nickname)
                }
            }
        }

        return@doInTransaction user
    }

    fun deleteUser(
        userId: String,
    ) = transactionManager.doInTransaction(TransactionIsolationLevel.REPEATABLE_READ) {
        val user = userRepository.findById(userId).get()
        user.delete(SccClock.instant())
        userRepository.save(user)

        userAuthInfoRepository.removeByUserId(userId)
    }

    fun getUser(userId: String): User? = transactionManager.doInTransaction {
        userRepository.findByIdOrNull(userId)
    }

    fun getUsers(userIds: List<String>): List<User> = transactionManager.doInTransaction {
        userRepository.findAllById(userIds).toList()
    }

    fun getAllUsers(): List<User> {
        return userRepository.findAll().toList()
    }

    private fun subscribeToNewsLetter(userId: String, email: String, name: String) {
        sccServerEventRecorder.record(NewsletterSubscribedOnSignupPayload(userId))
        runBlocking {
            stibeeSubscriptionService.registerSubscriber(
                email = email,
                name = name,
                // 일단 false 로 두지만 나중에 동의 버튼이 추가될 수도 있다
                isMarketingPushAgreed = false,
            )
        }
    }
}
