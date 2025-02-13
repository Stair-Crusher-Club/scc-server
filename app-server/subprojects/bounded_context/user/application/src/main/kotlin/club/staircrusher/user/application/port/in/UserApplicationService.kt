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
import club.staircrusher.user.application.port.out.persistence.UserAccountConnectionRepository
import club.staircrusher.user.application.port.out.persistence.UserAccountRepository
import club.staircrusher.user.application.port.out.persistence.UserAuthInfoRepository
import club.staircrusher.user.domain.model.AuthTokens
import club.staircrusher.user.application.port.out.persistence.UserProfileRepository
import club.staircrusher.user.application.port.out.web.subscription.StibeeSubscriptionService
import club.staircrusher.user.domain.model.UserAccount
import club.staircrusher.user.domain.model.UserAccountConnection
import club.staircrusher.user.domain.model.UserProfile
import club.staircrusher.user.domain.model.UserMobilityTool
import club.staircrusher.user.domain.model.UserAccountType
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
    private val userAccountRepository: UserAccountRepository,
    private val userProfileRepository: UserProfileRepository,
    private val userAccountConnectionRepository: UserAccountConnectionRepository,
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
        val params = UserProfileRepository.CreateUserParams(
            nickname = nickname,
            password = password,
            instagramId = instagramId,
            email = null,
        )

        val user = signUp(params)

        val accessToken = userAuthService.issueAccessToken(user)
        AuthTokens(accessToken)
    }

    fun signUp(params: UserProfileRepository.CreateUserParams): UserProfile {
        val normalizedNickname = params.nickname.trim()
        if (normalizedNickname.length < 2) {
            throw SccDomainException("최소 2자 이상의 닉네임을 설정해주세요.")
        }
        if (userProfileRepository.findFirstByNickname(normalizedNickname) != null) {
            throw SccDomainException("${normalizedNickname}은 이미 사용된 닉네임입니다.")
        }

        val id = EntityIdGenerator.generateRandom()
        userAccountRepository.save(
            UserAccount(
                id = id,
                accountType = UserAccountType.IDENTIFIED,
                createdAt = SccClock.instant(),
                updatedAt = SccClock.instant(),
            )
        )
        return userProfileRepository.save(
            UserProfile(
                id = id,
                nickname = normalizedNickname,
                encryptedPassword = params.password?.trim()?.let { passwordEncryptor.encrypt(it) },
                instagramId = params.instagramId?.trim()?.takeIf { it.isNotEmpty() },
                email = params.email,
                mobilityTools = mutableListOf(),
            )
        )
    }

    fun createAnonymousUser(): UserAccount {
        val id = EntityIdGenerator.generateRandom()
        return userAccountRepository.save(
            UserAccount(
                id = id,
                accountType = UserAccountType.ANONYMOUS,
                createdAt = SccClock.instant(),
                updatedAt = SccClock.instant(),
            )
        )
    }

    @Deprecated("닉네임 로그인은 사라질 예정")
    fun login(
        nickname: String,
        password: String
    ): AuthTokens = transactionManager.doInTransaction {
        val user = userProfileRepository.findFirstByNickname(nickname) ?: throw SccDomainException("잘못된 계정입니다.")
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
    ): UserProfile = transactionManager.doInTransaction {
        val user = userProfileRepository.findById(userId).get()
        user.pushToken = pushToken
        userProfileRepository.save(user)
    }

    fun sendPushNotification(
        userIds: List<String>,
        title: String?,
        body: String,
        deepLink: String?,
    ) = transactionManager.doInTransaction {
        val users = userProfileRepository.findAllById(userIds)
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
    ): UserProfile = transactionManager.doInTransaction(TransactionIsolationLevel.REPEATABLE_READ) {
        val user = userProfileRepository.findById(userId).get()
        user.nickname = run {
            val normalizedNickname = nickname.trim()
            if (normalizedNickname.length < 2) {
                throw SccDomainException(
                    "최소 2자 이상의 닉네임을 설정해주세요.",
                    SccDomainException.ErrorCode.INVALID_NICKNAME,
                )
            }
            if (userProfileRepository.findFirstByNickname(normalizedNickname)?.takeIf { it.id != user.id } != null) {
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
            if (userProfileRepository.findFirstByEmail(normalizedEmail)?.takeIf { it.id != user.id } != null) {
                throw SccDomainException(
                    "${normalizedEmail}은 이미 사용 중인 이메일입니다.",
                    SccDomainException.ErrorCode.INVALID_EMAIL,
                )
            }
            normalizedEmail
        }
        user.instagramId = instagramId?.trim()?.takeIf { it.isNotEmpty() }
        user.mobilityTools = mobilityTools
        userProfileRepository.save(user)

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
        val userProfile = userProfileRepository.findByIdOrNull(userId)
        val userAccount = userAccountRepository.findByIdOrNull(userId)

        val now = SccClock.instant()
        userProfile?.let {
            it.delete(now)
            userProfileRepository.save(it)
        }
        userAccount?.let {
            it.delete(now)
            userAccountRepository.save(it)
        }

        userAuthInfoRepository.removeByUserId(userId)
    }

    fun connectToIdentifiedAccount(anonymousUserId: String, identifiedUserId: String) {
        userAccountConnectionRepository.save(
            UserAccountConnection(
                id = EntityIdGenerator.generateRandom(),
                identifiedUserAccountId = identifiedUserId,
                anonymousUserAccountId = anonymousUserId,
            )
        )
    }

    fun getUser(userId: String): UserAccount? = transactionManager.doInTransaction {
        userAccountRepository.findByIdOrNull(userId)
    }

    fun getUserProfile(userId: String): UserProfile? = transactionManager.doInTransaction {
        userProfileRepository.findByIdOrNull(userId)
    }

    fun getUserProfiles(userIds: List<String>): List<UserProfile> = transactionManager.doInTransaction {
        userProfileRepository.findAllById(userIds).toList()
    }

    fun getAllUserProfiles(): List<UserProfile> {
        return userProfileRepository.findAll().toList()
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
