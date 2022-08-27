package club.staircrusher.accessibility.domain.service

import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.accessibility.domain.model.StairInfo
import club.staircrusher.accessibility.domain.repository.PlaceAccessibilityRepository
import club.staircrusher.stdlib.domain.DomainException
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import java.time.Clock

class PlaceAccessibilityService(
    private val clock: Clock,
    private val placeAccessibilityRepository: PlaceAccessibilityRepository,
    private val conquerRankingService: ConquerRankingService,
) {
    data class CreateParams(
        val placeId: String,
        val isFirstFloor: Boolean,
        val stairInfo: StairInfo,
        val hasSlope: Boolean,
        val userId: String?,
    )

    fun create(params: CreateParams): PlaceAccessibility {
        if (placeAccessibilityRepository.findByPlaceId(params.placeId) != null) {
            throw DomainException("이미 접근성 정보가 등록된 장소입니다.")
        }
        val result = placeAccessibilityRepository.save(
            PlaceAccessibility(
                id = EntityIdGenerator.generateRandom(),
                placeId = params.placeId,
                isFirstFloor = params.isFirstFloor,
                stairInfo = params.stairInfo,
                hasSlope = params.hasSlope,
                userId = params.userId,
                createdAt = clock.instant(),
            )
        )

        params.userId?.let { conquerRankingService.updateRanking(it) }

        return result
    }
}
