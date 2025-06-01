package club.staircrusher.place.domain.model.accessibility

enum class AccessibilityReportReason(val humanReadableName: String) {
    InaccurateInfo(humanReadableName = "틀린 정보가 있어요"),
    Closed(humanReadableName = "폐점된 곳이에요"),
    BadUser(humanReadableName = "이 정복자를 차단할래요"),
    None(humanReadableName = "알 수 없음"),
}
