package club.staircrusher.stdlib.geography

enum class CrsType(val wellKnownName: String, val proj: String) {
    // 위경도 좌표계 (aka WGS84)
    EPSG_4326("WGS84", "+proj=longlat +datum=WGS84 +no_defs"),
    // 공공 데이터 포털에서 사용하는 중부 원점 좌표계
    EPSG_5174("EPSG:5174","+proj=tmerc +lat_0=38 +lon_0=127.002890277778 +k=1 +x_0=200000 +y_0=500000 +ellps=bessel +towgs84=-145.907,505.034,685.756,-1.162,2.347,1.592,6.342 +units=m +no_defs")
}
