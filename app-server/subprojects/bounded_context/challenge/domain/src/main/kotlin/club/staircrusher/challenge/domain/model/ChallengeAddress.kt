package club.staircrusher.challenge.domain.model

import club.staircrusher.place.domain.model.Building
import club.staircrusher.place.domain.model.BuildingAddress
import club.staircrusher.place.domain.model.Place

data class ChallengeAddress(
    val siDo: String,
    val siGunGu: String,
    val eupMyeonDong: String,
    val li: String,
    val roadName: String
) {
    fun contains(keyword: String): Boolean {
        return siDo.contains(keyword) ||
            siGunGu.contains(keyword) ||
            eupMyeonDong.contains(keyword) ||
            li.contains(keyword) ||
            roadName.contains(keyword)
    }

    constructor(place: Place) : this(place.address)

    constructor(building: Building) : this(building.address)

    constructor(buildingAddress: BuildingAddress) : this(
        siDo = buildingAddress.siDo,
        siGunGu = buildingAddress.siGunGu,
        eupMyeonDong = buildingAddress.eupMyeonDong,
        li = buildingAddress.li,
        roadName = buildingAddress.roadName,
    )
}
