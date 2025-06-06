package club.staircrusher.place.application.port.`in`.accessibility

import club.staircrusher.place.application.port.out.accessibility.persistence.AccessibilityRankRepository
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class GetCountForNextRankUseCase(
    private val accessibilityRankRepository: AccessibilityRankRepository,
    private val transactionManager: TransactionManager,
) {
    fun handle(userId: String): Int  = transactionManager.doInTransaction {
        val currentRank = accessibilityRankRepository.findFirstByUserId(userId) ?: throw SccDomainException("잘못된 계정입니다.")
        val rank = currentRank.rank

        if (rank == 1L) {
            0
        } else {
            val nextRank = accessibilityRankRepository.findNextRank(rank)!!
            nextRank.conqueredCount - currentRank.conqueredCount
        }
    }
}
