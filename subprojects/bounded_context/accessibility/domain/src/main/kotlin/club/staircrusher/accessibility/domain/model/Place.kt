package club.staircrusher.accessibility.domain.model

/**
 * Value Object.
 */
data class Place(
    val id: String,
    val buildingId: String,
    val address: String,
) {
    val isAccessibilityRegistrable: Boolean
        get() {
            return address.startsWith("서울") || address.startsWith("경기 성남시")
        }
}
