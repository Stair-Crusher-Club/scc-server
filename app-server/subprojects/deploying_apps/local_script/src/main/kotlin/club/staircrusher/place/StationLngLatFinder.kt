package club.staircrusher.place

import club.staircrusher.findLngLat
import club.staircrusher.infra.network.RateLimiterFactory
import club.staircrusher.place.infra.adapter.out.web.KakaoMapsService
import club.staircrusher.place.infra.adapter.out.web.KakaoProperties
import io.micrometer.core.instrument.simple.SimpleMeterRegistry

private const val kakaoApiKey = "test key"
private val kakaoMapsService = KakaoMapsService(KakaoProperties(kakaoApiKey), RateLimiterFactory(SimpleMeterRegistry()))

val originalStationNames = listOf(
    "압구정로데오","여의도","을지로입구","혜화","숙대입구(갈월)","을지로3가","종각","서초","신논현","충무로","삼성중앙","경복궁(정부서울청사)","상왕십리","종로5가","봉천","영등포시장","동대문역사문화공원","서대문","중랑","광화문(세종문화회관)","회현(남대문시장)","을지로3가","남영","명동","신도림","암사","종로3가","안국","불광","종로3가","시청","용산","신설동","합정","신촌","시청","고속터미널","충무로","이태원","청량리(서울시립대입구)","서울숲","사가정","동대입구","마포","문래","미아사거리","신촌","동대문","수유(강북구청)","영등포","한성대입구(삼선교)","독산","서울역","동대문역사문화공원","신설동","연신내","신설동","구로디지털단지","가산디지털단지","청량리(서울시립대입구)","신당","종로3가","한남","선릉","총신대입구(이수)","서울대입구(관악구청)","국회의사당","동대문","성수","약수","영등포구청","왕십리(성동구청)","왕십리(성동구청)","등촌","금천구청","화곡","논현","오류동","잠실새내","성신여대입구(돈암)","회기","신림","동묘앞","신사","약수","효창공원앞","연신내","삼각지","신당","구의(광진구청)","쌍문","군자(능동)","신림","가산디지털단지","성신여대입구(돈암)","까치산","압구정","디지털미디어시티","한티","언주","역삼","여의도","상봉(시외버스터미널)","공덕","뚝섬","낙성대","녹사평(용산구청)","군자(능동)","삼성(무역센터)","불광","아차산(어린이대공원후문)","신대방삼거리","천호(풍납토성)","건대입구","천호(풍납토성)","사당","안암(고대병원앞)","서울대벤처타운","가양","동묘앞","둔촌동","신논현","구로","홍대입구","선릉","구파발","노량진","석촌","대치","강남구청","신정네거리","공릉(서울과학기술대)","학동","디지털미디어시티","발산","명일","영등포구청","방배","건대입구","강동","신사","길동","공덕","망원","남부터미널(예술의전당)","강남","홍대입구","홍대입구","신도림","선정릉","양재(서초구청)","송정","먹골","송파","이대","상도","상봉(시외버스터미널)","신용산","당산","석계","노원","잠실(송파구청)","송파나루","당산","매봉","창동","선정릉","사당","강남","석촌","교대(법원.검찰청)","상수","강남구청","논현","노원","노량진","수서","잠실(송파구청)","양재(서초구청)","문정","고덕","교대(법원.검찰청)","서울역","가락시장","가락시장","강변(동서울터미널)","오목교(목동운동장앞)","마곡",
)
fun main() {
    val lngLatResults = kakaoMapsService.findLngLat(originalStationNames.map {
        if ("(" in it) {
            it.split("(")[0]
        } else {
            it
        } + "역"
    })
    println(lngLatResults)
    println(buildString {
        appendLine("지하철역,lng,lat")
        originalStationNames.zip(lngLatResults).forEach { (originalStationName, lngLatResult) ->
            appendLine("$originalStationName,${lngLatResult.lng},${lngLatResult.lat}")
        }
    })
}
