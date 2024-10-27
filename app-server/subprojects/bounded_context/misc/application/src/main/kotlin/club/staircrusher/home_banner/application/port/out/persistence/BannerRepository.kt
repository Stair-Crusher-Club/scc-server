package club.staircrusher.home_banner.application.port.out.persistence

import club.staircrusher.home_banner.domain.model.Banner
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.Instant

interface BannerRepository : CrudRepository<Banner, String> {
    @Query(
        """
        SELECT
            b
        FROM
            Banner b
        WHERE
            (b.startAt IS NULL OR b.startAt < :now)
            AND (b.endAt IS NULL OR b.endAt > :now)
        ORDER BY
            b.displayOrder ASC,
            b.createdAt DESC
    """)
    fun findActiveHomeBanners(now: Instant): List<Banner>
}
