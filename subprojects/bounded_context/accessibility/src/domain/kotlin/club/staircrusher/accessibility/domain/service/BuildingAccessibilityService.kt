package club.staircrusher.accessibility.domain.service

import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.StairInfo
import club.staircrusher.accessibility.domain.repository.BuildingAccessibilityRepository
import club.staircrusher.stdlib.domain.DomainException
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import java.time.Clock

class BuildingAccessibilityService(
    private val clock: Clock,
    private val buildingAccessibilityRepository: BuildingAccessibilityRepository,
) {
    data class CreateParams(
        val buildingId: String,
        val entranceStairInfo: StairInfo,
        val hasSlope: Boolean,
        val hasElevator: Boolean,
        val elevatorStairInfo: StairInfo,
        val userId: String?,
    )

    fun create(params: CreateParams): BuildingAccessibility {
        if (buildingAccessibilityRepository.findByBuildingId(params.buildingId) != null) {
            throw DomainException("이미 접근성 정보가 등록된 건물입니다.")
        }
        if (params.hasElevator && params.elevatorStairInfo == StairInfo.UNDEFINED ||
            !params.hasElevator && params.elevatorStairInfo != StairInfo.UNDEFINED) {
            throw DomainException("엘레베이터 유무 정보와 엘레베이터까지의 계단 개수 정보가 맞지 않습니다.") // TODO: 테스트 추가
        }
        return buildingAccessibilityRepository.save(
            BuildingAccessibility(
                id = EntityIdGenerator.generateRandom(),
                buildingId = params.buildingId,
                entranceStairInfo = params.entranceStairInfo,
                hasSlope = params.hasSlope,
                hasElevator = params.hasElevator,
                elevatorStairInfo = params.elevatorStairInfo,
                userId = params.userId,
                createdAt = clock.instant(),
            )
        )
    }
}
