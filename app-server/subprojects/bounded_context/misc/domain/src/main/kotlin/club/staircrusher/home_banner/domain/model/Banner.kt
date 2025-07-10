package club.staircrusher.home_banner.domain.model

import club.staircrusher.stdlib.clock.SccClock
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.domain.Sort
import java.time.Instant

@Entity
@Table(name = "home_banner") // TODO: rename table?
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
        require(loggingKey.isNotBlank()) {
            "로깅 키가 비어 있습니다."
        }
        require(imageUrl.isNotBlank()) {
            "배너 이미지 URL이 비어 있습니다."
        }
        require(loggingKey.isNotBlank()) {
            "랜딩 페이지 URL이 비어 있습니다."
        }
        require(clickPageTitle.isNotBlank()) {
            "랜딩 페이지 제목이 비어 있습니다."
        }
        require(clickPageTitle.length <= 32) {
            "랜딩 페이지 제목은 32자를 초과할 수 없습니다."
        }
        require((startAt ?: Instant.MIN) < (endAt ?: Instant.MAX)) {
            "배너 종료 시각이 시작 시각 이후로 설정되어야 합니다."
        }
        require(SccClock.instant() < (endAt ?: Instant.MAX)) {
            "배너 종료 시각이 현재 시각 이후로 설정되어야 합니다."
        }
    }

    var startAt: Instant? = startAt
        protected set

    var endAt: Instant? = endAt
        protected set

    var displayOrder: Int = displayOrder
        protected set

    val createdAt: Instant = SccClock.instant()

    companion object {
        val displaySort = Sort.by(Sort.Order.asc("displayOrder"), Sort.Order.desc("createdAt"))
    }
}
