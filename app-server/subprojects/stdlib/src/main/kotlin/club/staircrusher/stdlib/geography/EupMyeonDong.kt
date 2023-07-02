package club.staircrusher.stdlib.geography

// SiGunGu는 DB에 저장하지 않고 TSV를 파싱해서 in-memory로 관리한다.
data class EupMyeonDong(
    val id: String,
    val name: String,
    val siGunGu: SiGunGu,
)
