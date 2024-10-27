package club.staircrusher.home_banner.domain.model

import club.staircrusher.stdlib.clock.SccClock
import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.Instant

@Entity
class Banner(
    @Id val id: String,
    val loggingKey: String,
    val imageUrl: String,
    val clickPageUrl: String,
    val clickPageTitle: String,
    startAt: Instant?,
    endAt: Instant?,
    displayOrder: Int,
) {
    init {
        check((startAt ?: Instant.MIN) < (endAt ?: Instant.MAX)) {
            "배너 종료 시각이 시작 시각 이후로 설정되어야 합니다."
        }
    }

    var startAt: Instant? = startAt
        protected set

    var endAt: Instant? = endAt
        protected set

    var displayOrder: Int = displayOrder
        protected set

    val createdAt: Instant = SccClock.instant()
}
