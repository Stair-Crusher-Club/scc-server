package club.staircrusher.user.application.port.`in`.use_case

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.application.port.out.persistence.UserAccountRepository
import club.staircrusher.user.application.port.out.persistence.UserProfileRepository
import club.staircrusher.user.domain.model.UserAccount
import club.staircrusher.user.domain.model.UserAccountType
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

@Component
class MigrateToUserAccountUseCase(
    private val transactionManager: TransactionManager,
    private val userProfileRepository: UserProfileRepository,
    private val userAccountRepository: UserAccountRepository,
) {
    fun handle() {
        var pageRequest = PageRequest.of(0, PAGE_SIZE, Sort.by(Sort.Order.asc("createdAt")))
        do {
            val page = transactionManager.doInTransaction(isReadOnly = true) {
                userProfileRepository.findAll(pageRequest)
            }

            page.content.forEach { userProfile ->
                transactionManager.doInTransaction {
                    if (userAccountRepository.existsById(userProfile.id)) {
                        return@doInTransaction
                    }
                    // 이미 발급되어 있는 JWT 의 하위 호환성을 맞춰주려면 user id 를 맞춰줘야 한다
                    val userAccount = UserAccount(
                        id = userProfile.id,
                        accountType = UserAccountType.IDENTIFIED,
                        // TODO: 마이그레이션을 마친 뒤에 TimeAuditingBaseEntity 로 대체
                        createdAt = userProfile.createdAt,
                        updatedAt = userProfile.updatedAt,
                    )

                    if (userProfile.isDeleted) {
                        // TODO: 마이그레이션을 마친 뒤에 deletedAt 을 다시 private 으로 바꾸기
                        userAccount.delete(userProfile.deletedAt!!)
                    }

                    userAccountRepository.save(userAccount)
                }
            }
            pageRequest = pageRequest.next()
        } while (page.hasNext())
    }

    companion object {
        private const val PAGE_SIZE = 50
    }
}
