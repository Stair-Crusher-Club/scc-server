package club.staircrusher.place.application.port.`in`.accessibility

import club.staircrusher.place.application.port.out.accessibility.persistence.BuildingAccessibilityRepository
import club.staircrusher.place.domain.model.accessibility.EntranceDoorType
import club.staircrusher.place.domain.model.accessibility.StairHeightLevel
import club.staircrusher.place.domain.model.accessibility.StairInfo
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class AdminUpdateBuildingAccessibilityUseCase(
    private val transactionManager: TransactionManager,
    private val buildingAccessibilityRepository: BuildingAccessibilityRepository,
) {
    fun handle(
        buildingAccessibilityId: String,
        entranceStairInfo: StairInfo,
        hasSlope: Boolean,
        hasElevator: Boolean,
        elevatorStairInfo: StairInfo,
        entranceStairHeightLevel: StairHeightLevel?,
        entranceDoorTypes: List<EntranceDoorType>?,
        elevatorStairHeightLevel: StairHeightLevel?
    ) = transactionManager.doInTransaction {
        val buildingAccessibility = buildingAccessibilityRepository.findById(buildingAccessibilityId).get()

        // 0401 이전 버전에서는 entranceDoorTypes, entranceStairHeightLevel, elevatorStairHeightLevel 를 올려줄 수 없다.
        // entranceDoorTypes 은 필수타입이라 없으면 예전버전이라고 판단
        if (listOf(entranceDoorTypes, entranceStairHeightLevel, elevatorStairHeightLevel).all { it == null }) {
            buildingAccessibility.apply {
                this.entranceStairInfo = entranceStairInfo
                this.hasSlope = hasSlope
                this.hasElevator = hasElevator
                this.elevatorStairInfo = elevatorStairInfo
            }

            return@doInTransaction buildingAccessibilityRepository.save(buildingAccessibility)
        }

        if (entranceStairInfo == StairInfo.ONE && entranceStairHeightLevel == null) {
            throw SccDomainException("입구 계단 높이를 입력해주세요.")
        }

        if (elevatorStairInfo == StairInfo.ONE && elevatorStairHeightLevel == null) {
            throw SccDomainException("엘리베이터 계단 높이를 입력해주세요.")
        }

        if (entranceDoorTypes!!.contains(EntranceDoorType.None) && entranceDoorTypes.size > 1) {
            throw SccDomainException("문이 없는 경우 다른 문 유형을 선택할 수 없습니다.")
        }

        buildingAccessibility.apply {
            this.entranceStairInfo = entranceStairInfo
            this.hasSlope = hasSlope
            this.hasElevator = hasElevator
            this.elevatorStairInfo = elevatorStairInfo
            this.entranceStairHeightLevel = entranceStairHeightLevel
            this.entranceDoorTypes = entranceDoorTypes
            this.elevatorStairHeightLevel = elevatorStairHeightLevel
        }

        return@doInTransaction buildingAccessibilityRepository.save(buildingAccessibility)
    }
}
