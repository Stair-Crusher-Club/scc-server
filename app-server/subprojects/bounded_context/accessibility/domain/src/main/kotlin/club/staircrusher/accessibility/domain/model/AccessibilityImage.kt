package club.staircrusher.accessibility.domain.model

data class AccessibilityImage(
    val type: Type,
    val imageUrl: String,
    var thumbnailUrl: String? = null,
) {
    enum class Type {
        PLACE,
        BUILDING_ENTRANCE,
        BUILDING_ELEVATOR,
    }
}
