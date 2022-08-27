package club.staircrusher.stdlib.geography

// SiGunGu는 DB에 저장하지 않고 TSV를 파싱해서 in-memory로 관리한다.
data class SiGunGu(
    val id: String,
    val name: String,
    // 시도는 굳이 엔티티로 만들 필요가 없어서 string으로 대체한다.
    val siDo: String,
)
