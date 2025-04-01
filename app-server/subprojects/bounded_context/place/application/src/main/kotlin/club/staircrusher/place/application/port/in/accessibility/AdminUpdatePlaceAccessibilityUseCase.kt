package club.staircrusher.place.application.port.`in`.accessibility

import club.staircrusher.place.application.port.out.accessibility.persistence.PlaceAccessibilityRepository
import club.staircrusher.place.domain.model.accessibility.EntranceDoorType
import club.staircrusher.place.domain.model.accessibility.PlaceAccessibility
import club.staircrusher.place.domain.model.accessibility.StairHeightLevel
import club.staircrusher.place.domain.model.accessibility.StairInfo
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class AdminUpdatePlaceAccessibilityUseCase(
    private val transactionManager: TransactionManager,
    private val placeAccessibilityRepository: PlaceAccessibilityRepository,
) {
    fun handle(
        placeAccessibilityId: String,
        isFirstFloor: Boolean,
        stairInfo: StairInfo,
        hasSlope: Boolean,
        floors: List<Int>?,
        isStairOnlyOption: Boolean?,
        stairHeightLevel: StairHeightLevel?,
        entranceDoorTypes: List<EntranceDoorType>?,
    ): PlaceAccessibility = transactionManager.doInTransaction {
        val placeAccessibility = placeAccessibilityRepository.findById(placeAccessibilityId).get()

        // 0401 이전 버전에서 등록된 장소의 경우 floors, stairHeightLevel, entranceDoorTypes 가 null 이다
        if (listOf(floors, isStairOnlyOption, stairHeightLevel, entranceDoorTypes).all { it == null }) {
            placeAccessibility.apply {
                this.isFirstFloor = isFirstFloor
                this.stairInfo = stairInfo
                this.hasSlope = hasSlope
            }

            return@doInTransaction placeAccessibilityRepository.save(placeAccessibility)
        }

        // 0401 이후 버전에서는 floors, entranceDoorTypes 는 필수 / isStairOnlyOption 는 floors 에 따라, stairHeightLevel 은 stairInfo 에 따라 입력을 받는다.
        if (floors == null || entranceDoorTypes == null) {
            throw SccDomainException("층 정보와 출입문 유형은 필수입니다.")
        }

        if (floors.size > 1 && isStairOnlyOption == null) {
            throw SccDomainException("다른 층으로 가는 정보를 입력해주세요.")
        }

        if (isFirstFloor && floors.contains(1).not()) {
            throw SccDomainException("1층이 포함되어 있지 않습니다.")
        }

        if (stairInfo == StairInfo.ONE && stairHeightLevel == null) {
            throw SccDomainException("계단 높이를 입력해주세요.")
        }

        if (entranceDoorTypes.contains(EntranceDoorType.None) && entranceDoorTypes.size > 1) {
            throw SccDomainException("문이 없는 경우 다른 문 유형을 선택할 수 없습니다.")
        }

        placeAccessibility.apply {
            this.floors = floors
            this.isFirstFloor = isFirstFloor
            this.isStairOnlyOption = isStairOnlyOption
            this.stairInfo = stairInfo
            this.stairHeightLevel = stairHeightLevel
            this.hasSlope = hasSlope
            this.entranceDoorTypes = entranceDoorTypes
        }

        return@doInTransaction placeAccessibilityRepository.save(placeAccessibility)
    }
}
