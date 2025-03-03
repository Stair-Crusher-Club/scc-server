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
import club.staircrusher.stdlib.time.getYear
import club.staircrusher.stdlib.validation.email.EmailValidator
import club.staircrusher.user.application.port.out.persistence.UserAccountConnectionRepository
import club.staircrusher.user.application.port.out.persistence.UserAccountRepository
import club.staircrusher.user.application.port.out.persistence.UserAuthInfoRepository
import club.staircrusher.user.domain.model.AuthTokens
import club.staircrusher.user.application.port.out.persistence.UserProfileRepository
import club.staircrusher.user.application.port.out.web.subscription.StibeeSubscriptionService
import club.staircrusher.user.domain.model.IdentifiedUserVO
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
            birthYear = null,
        )

        val (userAccount, userProfile) = signUp(params)

        val accessToken = userAuthService.issueAccessToken(userAccount)
        AuthTokens(accessToken)
    }

    fun signUp(params: UserProfileRepository.CreateUserParams): IdentifiedUserVO {
        val normalizedNickname = params.nickname.trim()
        if (normalizedNickname.length < 2) {
            throw SccDomainException("최소 2자 이상의 닉네임을 설정해주세요.")
        }
        if (userProfileRepository.findFirstByNickname(normalizedNickname) != null) {
            throw SccDomainException("${normalizedNickname}은 이미 사용된 닉네임입니다.")
        }

        val id = EntityIdGenerator.generateRandom()
        val userAccount = userAccountRepository.save(
            UserAccount(
                id = id,
                accountType = UserAccountType.IDENTIFIED,
            )
        )
        val userProfile = userProfileRepository.save(
            UserProfile(
                id = id,
                // TODO: userProfile 와 userAccount 의 사용처를 완전히 분리한 뒤 다른 각자의 id 로 관리 할 수 있다
                userId = id,
                nickname = normalizedNickname,
                encryptedPassword = params.password?.trim()?.let { passwordEncryptor.encrypt(it) },
                instagramId = params.instagramId?.trim()?.takeIf { it.isNotEmpty() },
                email = params.email,
                mobilityTools = mutableListOf(),
                birthYear = params.birthYear,
            )
        )

        return IdentifiedUserVO(userAccount, userProfile)
    }

    fun createAnonymousUser(): UserAccount {
        val id = EntityIdGenerator.generateRandom()
        return userAccountRepository.save(
            UserAccount(
                id = id,
                accountType = UserAccountType.ANONYMOUS,
            )
        )
    }

    @Deprecated("닉네임 로그인은 사라질 예정")
    fun login(
        nickname: String,
        password: String
    ): AuthTokens = transactionManager.doInTransaction {
        val userProfile = userProfileRepository.findFirstByNickname(nickname) ?: throw SccDomainException("잘못된 계정입니다.")
        val userAccount = userAccountRepository.findByIdOrNull(userProfile.userId) ?: throw SccDomainException("잘못된 계정입니다.")
        if (userAccount.isDeleted || userProfile.isDeleted) {
            throw SccDomainException("잘못된 계정입니다.")
        }
        if (!passwordEncryptor.verify(password, userProfile.encryptedPassword ?: "")) {
            throw SccDomainException("잘못된 비밀번호입니다.")
        }
        val accessToken = userAuthService.issueAccessToken(userAccount)
        AuthTokens(accessToken)
    }

    fun updatePushToken(
        userId: String,
        pushToken: String,
    ): UserProfile = transactionManager.doInTransaction {
        val userProfile = userProfileRepository.findFirstByUserId(userId) ?: throw SccDomainException("잘못된 계정입니다.")
        userProfile.pushToken = pushToken
        userProfileRepository.save(userProfile)
    }

    fun sendPushNotification(
        userIds: List<String>,
        title: String?,
        body: String,
        deepLink: String?,
    ) = transactionManager.doInTransaction {
        val userProfiles = userProfileRepository.findAllByUserIdIn(userIds)
        val notifications = userProfiles.mapNotNull { userProfile ->
            userProfile.pushToken ?: return@mapNotNull null
            userProfile.pushToken!! to PushService.Notification(
                // just poc for now, but not sure this substitution needs to be placed here
                title = title?.replace("{{nickname}}", userProfile.nickname),
                body = body.replace("{{nickname}}", userProfile.nickname),
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
        birthYear: Int?,
        isNewsLetterSubscriptionAgreed: Boolean,
    ): UserProfile = transactionManager.doInTransaction(TransactionIsolationLevel.REPEATABLE_READ) {
        val userProfile = userProfileRepository.findFirstByUserId(userId) ?: throw SccDomainException("잘못된 계정입니다.")
        userProfile.nickname = run {
            val normalizedNickname = nickname.trim()
            if (normalizedNickname.length < 2) {
                throw SccDomainException(
                    "최소 2자 이상의 닉네임을 설정해주세요.",
                    SccDomainException.ErrorCode.INVALID_NICKNAME,
                )
            }
            if (userProfileRepository.findFirstByNickname(normalizedNickname)?.takeIf { it.id != userProfile.id } != null) {
                throw SccDomainException(
                    "${normalizedNickname}은 이미 사용 중인 닉네임입니다.",
                    SccDomainException.ErrorCode.INVALID_NICKNAME,
                )
            }
            normalizedNickname
        }
        userProfile.email = run {
            val normalizedEmail = email.trim()
            if (!EmailValidator.isValid(normalizedEmail)) {
                throw SccDomainException(
                    "${normalizedEmail}은 유효한 형태의 이메일이 아닙니다.",
                    SccDomainException.ErrorCode.INVALID_EMAIL,
                )
            }
            if (userProfileRepository.findFirstByEmail(normalizedEmail)?.takeIf { it.id != userProfile.id } != null) {
                throw SccDomainException(
                    "${normalizedEmail}은 이미 사용 중인 이메일입니다.",
                    SccDomainException.ErrorCode.INVALID_EMAIL,
                )
            }
            normalizedEmail
        }
        userProfile.instagramId = instagramId?.trim()?.takeIf { it.isNotEmpty() }
        userProfile.mobilityTools = mobilityTools
        userProfile.birthYear = run {
            birthYear?.let {
                if (it < 1900 || it > SccClock.instant().getYear()) {
                    throw SccDomainException(
                        "출생 연도가 유효하지 않습니다.",
                        SccDomainException.ErrorCode.INVALID_BIRTH_YEAR,
                    )
                }
            }
            birthYear
        }
        userProfileRepository.save(userProfile)

        if (isNewsLetterSubscriptionAgreed) {
            userProfile.email?.let {
                transactionManager.doAfterCommit {
                    subscribeToNewsLetter(userId, it, userProfile.nickname)
                }
            }
        }

        return@doInTransaction userProfile
    }

    fun deleteUser(
        userId: String,
    ) = transactionManager.doInTransaction(TransactionIsolationLevel.REPEATABLE_READ) {
        val userAccount = userAccountRepository.findByIdOrNull(userId)
        val userProfile = userProfileRepository.findFirstByUserId(userId)

        val now = SccClock.instant()
        userAccount?.let {
            it.delete(now)
            userAccountRepository.save(it)
        }
        userProfile?.let {
            it.delete(now)
            userProfileRepository.save(it)
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

    fun getAccountOrNull(userId: String): UserAccount? = transactionManager.doInTransaction {
        userAccountRepository.findByIdOrNull(userId)
    }

    fun getProfileByUserIdOrNull(userId: String): UserProfile? = transactionManager.doInTransaction {
        userProfileRepository.findFirstByUserId(userId)
    }

    fun getProfilesByUserIds(userIds: List<String>): List<UserProfile> = transactionManager.doInTransaction {
        userProfileRepository.findAllByUserIdIn(userIds)
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
