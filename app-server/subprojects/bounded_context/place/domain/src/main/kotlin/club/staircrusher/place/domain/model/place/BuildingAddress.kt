package club.staircrusher.place.domain.model.place

import jakarta.persistence.Embeddable

@Embeddable
data class BuildingAddress(
    val siDo: String,
    val siGunGu: String,
    val eupMyeonDong: String,
    val li: String,
    val roadName: String,
    val mainBuildingNumber: String,
    val subBuildingNumber: String,
) {
    override fun toString(): String {
        // refs: https://www.juso.go.kr/CommonPageLink.do?link=/street/GuideBook
        return buildString {
            append("$siDo $siGunGu ")
            if (!eupMyeonDong.endsWith("동")) {
                append("$eupMyeonDong ")
            }
            append("$roadName ")
            append(mainBuildingNumber)
            if (subBuildingNumber.isNotBlank() && subBuildingNumber != "0") {
                append("-$subBuildingNumber")
            }
        }
    }
}
