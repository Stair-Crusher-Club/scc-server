package club.staircrusher.home_banner.application.port.out.persistence

import club.staircrusher.home_banner.domain.model.Banner
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface BannerRepository : JpaRepository<Banner, String> {
    @Query(
        """
        SELECT
            b
        FROM
            Banner b
        WHERE
            (b.startAt IS NULL OR b.startAt < :now)
            AND (b.endAt IS NULL OR b.endAt > :now)
    """)
    fun findActiveHomeBanners(now: Instant, sort: Sort): List<Banner>
}
