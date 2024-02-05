package club.staircrusher.stdlib.place

enum class PlaceCategory(val humanReadableName: String) {
    MARKET(humanReadableName = "대형마트"),
    CONVENIENCE_STORE(humanReadableName = "편의점"),
    KINDERGARTEN(humanReadableName = "어린이집, 유치원"),
    SCHOOL(humanReadableName = "학교"),
    ACADEMY(humanReadableName = "학원"),
    PARKING_LOT(humanReadableName = "주차장"),
    GAS_STATION(humanReadableName = "주유소, 충전소"),
    SUBWAY_STATION(humanReadableName = "지하철역"),
    BANK(humanReadableName = "은행"),
    CULTURAL_FACILITIES(humanReadableName = "문화시설"),
    AGENCY(humanReadableName = "중개업소"),
    PUBLIC_OFFICE(humanReadableName = "공공기관"),
    ATTRACTION(humanReadableName = "관광명소"),
    ACCOMODATION(humanReadableName = "숙박"),
    RESTAURANT(humanReadableName = "음식점"),
    CAFE(humanReadableName = "카페"),
    HOSPITAL(humanReadableName = "병원"),
    PHARMACY(humanReadableName = "약국"),
}
