package club.staircrusher.quest.application.port.`in`

import club.staircrusher.place.application.port.`in`.PlaceApplicationService
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class ClubQuestSetIsClosedUseCase(
    private val transactionManager: TransactionManager,
    private val placeApplicationService: PlaceApplicationService,
) {
    fun handle(
        placeId: String,
        isClosed: Boolean,
    ) = transactionManager.doInTransaction {
        try {
            placeApplicationService.setIsClosed(placeId, isClosed)
        } catch (e: IllegalArgumentException) {
            // ignore
        }
    }
}
