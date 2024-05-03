package club.staircrusher.accessibility.domain.model


object AccessibilityScore {
    fun get(pa: PlaceAccessibility, ba: BuildingAccessibility?): Double? {
        return when {
            pa.isFirstFloor -> pa.score
            ba == null -> null
            ba.hasElevator -> ba.entranceScore plusOnNotNull ba.elevatorScore plusOnNotNull pa.score
            else -> ba.entranceScore plusOnNotNull NO_ELEVATOR_PENALTY plusOnNotNull pa.score
        }
    }

    private val PlaceAccessibility.score: Double?
        get() = StairInfos(stairInfo, stairHeightLevel, hasSlope).accessibilityScore
    private val BuildingAccessibility.entranceScore: Double?
        get() = StairInfos(entranceStairInfo, entranceStairHeightLevel, hasSlope).accessibilityScore
    private val BuildingAccessibility.elevatorScore: Double?
        // 엘레베이터 까지 도달하기 위한 경로에 Slope 가 있는지는 별도로 저장하지 않아서, 우선 건물 입구 Slope 를 사용한다.
        get() = StairInfos(elevatorStairInfo, elevatorStairHeightLevel, hasSlope).accessibilityScore
    private const val NO_ELEVATOR_PENALTY = 5.0

    private infix fun Double?.plusOnNotNull(other: Double?): Double? =
        if (this != null && other != null) this + other else null

    private data class StairInfos(
        val stairInfo: StairInfo,
        val stairHeightLevel: StairHeightLevel?,
        val hasSlope: Boolean,
    ) {
        val accessibilityScore: Double? = when (stairInfo) {
            StairInfo.NONE -> 0
            StairInfo.ONE ->
                when (stairHeightLevel) {
                    StairHeightLevel.HALF_THUMB -> 1
                    StairHeightLevel.THUMB, null -> 2
                    StairHeightLevel.OVER_THUMB -> 3
                }

            StairInfo.TWO_TO_FIVE -> 4
            StairInfo.OVER_SIX -> 6
            StairInfo.UNDEFINED -> null
        }?.times(if (hasSlope) 0.5 else 1.0)
    }
}

