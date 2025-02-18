package club.staircrusher.user.application.port.`in`.use_case

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.application.port.out.persistence.UserAccountRepository
import club.staircrusher.user.application.port.out.persistence.UserProfileRepository
import mu.KotlinLogging
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull

@Component
class MigrateToUserAccountUseCase(
    private val transactionManager: TransactionManager,
    private val userProfileRepository: UserProfileRepository,
    private val userAccountRepository: UserAccountRepository,
) {
    private val logger = KotlinLogging.logger {}

    fun handle() {
        logger.info { "Migration start" }
        var pageRequest = PageRequest.of(0, PAGE_SIZE, Sort.by(Sort.Order.asc("createdAt")))
        do {
            val page = transactionManager.doInTransaction(isReadOnly = true) {
                userProfileRepository.findAll(pageRequest)
            }

            page.content.forEach { userProfile ->
                transactionManager.doInTransaction {
                    if (userAccountRepository.existsById(userProfile.id).not()) {
                        logger.error { "UserAccount not found with id '${userProfile.id}'" }
                        return@doInTransaction
                    }
                    val reloadedUserProfile = userProfileRepository.findByIdOrNull(userProfile.id)
                    if (reloadedUserProfile == null) {
                        logger.error { "UserProfile not found with id '${userProfile.id}'" }
                        return@doInTransaction
                    }

                    // UserAccount 와 UserProfile 이 혼재되어 사용되는 기간이 존재하므로, 통일된 id 를 사용한다
                    reloadedUserProfile.userId = userProfile.id
                    userProfileRepository.save(reloadedUserProfile)
                }
            }
            pageRequest = pageRequest.next()
        } while (page.hasNext())

        logger.info { "Migration end" }
    }

    companion object {
        private const val PAGE_SIZE = 50
    }
}
