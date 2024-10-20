package club.staircrusher.home_banner.application.port.out.persistence

import club.staircrusher.home_banner.domain.model.HomeBanner
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.Instant

interface HomeBannerRepository : CrudRepository<HomeBanner, String> {
    @Query("""
        SELECT
            b
        FROM
            HomeBanner b
        WHERE
            (b.startAt IS NULL OR b.startAt < :now)
            AND (b.endAt IS NULL OR b.endAt > :now)
        ORDER BY
            b.displayOrder ASC,
            b.createdAt DESC
    """)
    fun findActiveHomeBanners(now: Instant): List<HomeBanner>
}
