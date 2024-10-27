package club.staircrusher.accessibility.domain.model


object AccessibilityScore {
    fun get(pa: PlaceAccessibility, ba: BuildingAccessibility?): Double? {
        return when {
            pa.isFirstFloor -> pa.score
            ba == null -> null
            ba.hasElevator -> getTotalScore(listOfNotNull(pa.score, ba.entranceScore, ba.elevatorScore))

            else -> 5.0
        }
    }

    private fun getTotalScore(scores: List<Double>): Double? =
        if (scores.isEmpty()) null
        else when {
            scores.max() == 0.0 -> 0.0
            scores.max() <= 1.0 -> 1.0
            scores.max() <= 2.0 -> 2.0
            scores.max() <= 3.0 && scores.count { score -> score == 3.0 } == 1 -> 3.0
            scores.max() <= 3.0 -> 4.0
            else -> 5.0
        }

    private val PlaceAccessibility.score: Double?
        get() = StairInfos(stairInfo, stairHeightLevel, hasSlope).accessibilityScore
    private val BuildingAccessibility.entranceScore: Double?
        get() = StairInfos(entranceStairInfo, entranceStairHeightLevel, hasSlope).accessibilityScore
    private val BuildingAccessibility.elevatorScore: Double?
        // 엘레베이터 까지 도달하기 위한 경로에 Slope 가 있는지는 별도로 저장하지 않아서, 우선 건물 입구 Slope 를 사용한다.
        get() = StairInfos(elevatorStairInfo, elevatorStairHeightLevel, hasSlope).accessibilityScore

    private data class StairInfos(
        val stairInfo: StairInfo,
        val stairHeightLevel: StairHeightLevel?,
        val hasSlope: Boolean,
    ) {
        val accessibilityScore: Double? = if (hasSlope) {
            if (stairInfo == StairInfo.NONE) 0.0 else 1.0
        } else {
            when (stairInfo) {
                StairInfo.NONE -> 0.0
                StairInfo.ONE -> 2.0
                StairInfo.TWO_TO_FIVE -> 3.0
                StairInfo.OVER_SIX -> 5.0
                StairInfo.UNDEFINED -> null
            }
        }
    }
}

