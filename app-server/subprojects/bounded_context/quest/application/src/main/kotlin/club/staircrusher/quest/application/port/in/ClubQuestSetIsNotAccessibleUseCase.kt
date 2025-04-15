package club.staircrusher.quest.application.port.`in`

import club.staircrusher.place.application.port.`in`.place.PlaceApplicationService
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class ClubQuestSetIsNotAccessibleUseCase(
    private val transactionManager: TransactionManager,
    private val placeApplicationService: PlaceApplicationService,
) {
    fun handle(
        placeId: String,
        isNotAccessible: Boolean,
    ) = transactionManager.doInTransaction {
        try {
            placeApplicationService.setIsNotAccessible(placeId, isNotAccessible)
        } catch (e: IllegalArgumentException) {
            // ignore
        }
    }
}
