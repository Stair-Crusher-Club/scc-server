package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.persistence.AccessibilityRankRepository
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class GetCountForNextRankUseCase(
    private val accessibilityRankRepository: AccessibilityRankRepository,
    private val transactionManager: TransactionManager,
) {
    fun handle(userId: String): Int  = transactionManager.doInTransaction {
        val currentRank = accessibilityRankRepository.findByUserId(userId) ?: throw SccDomainException("잘못된 계정입니다.")
        val rank = currentRank.rank ?: accessibilityRankRepository.findByConqueredCount(currentRank.conqueredCount)?.rank!!

        if (rank == 1L) {
            0
        } else {
            val nextRank = accessibilityRankRepository.findByRank(rank - 1)!!
            nextRank.conqueredCount - currentRank.conqueredCount
        }
    }
}
