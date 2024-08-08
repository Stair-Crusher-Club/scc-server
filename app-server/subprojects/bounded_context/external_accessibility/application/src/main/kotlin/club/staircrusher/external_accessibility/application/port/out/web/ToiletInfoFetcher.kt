package club.staircrusher.external_accessibility.application.port.out.web

interface ToiletInfoFetcher {
    fun fetchRecords(): List<ToiletRow>

    @Suppress("NonAsciiCharacters")
    data class ToiletRow(
        val toiletId: String,
        val toiletName: String,
        val imageUrl: String,
        val addressOld: String?,
        val addressNew: String?,
        val 이용성별: String?,
        val 추천접근로: String?,
        val 사용가능여부: String?,
        val 화장실입구구조: String?,
        val 내부가로너비: String?,
        val 내부세로너비: String?,
        val 대변기출입문: String?,
        val 대변기옆공간: String?,
        val 세면대아래공간: String?,
        val 세면대손잡이: String?,
        val 기타참고사항: String?,
        val 상세주소: String?,
        val latitude: Double,
        val longitude: Double,
    )
}
