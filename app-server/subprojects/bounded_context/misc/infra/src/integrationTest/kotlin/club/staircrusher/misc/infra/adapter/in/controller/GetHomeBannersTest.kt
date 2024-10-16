package club.staircrusher.misc.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.GetHomeBannersResponseDto
import club.staircrusher.home_banner.application.port.out.persistence.HomeBannerRepository
import club.staircrusher.home_banner.domain.model.HomeBanner
import club.staircrusher.misc.infra.adapter.`in`.controller.base.MiscITBase
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration

class GetHomeBannersTest : MiscITBase() {
    @Autowired
    private lateinit var homeBannerRepository: HomeBannerRepository

    @BeforeEach
    fun setUp() {
        transactionManager.doInTransaction {
            homeBannerRepository.deleteAll()
        }
    }

    @Test
    fun `active 배너가 displayOrder 순으로 내려온다`() {
        val now = SccClock.instant()
        val activeBanners = listOf(
            HomeBanner(
                id = EntityIdGenerator.generateRandom(),
                loggingKey = "",
                imageUrl = "",
                clickPageUrl = "",
                clickPageTitle = "",
                startAt = now - Duration.ofHours(1),
                endAt = null,
                displayOrder = 3,
            ),
            HomeBanner(
                id = EntityIdGenerator.generateRandom(),
                loggingKey = "",
                imageUrl = "",
                clickPageUrl = "",
                clickPageTitle = "",
                startAt = now - Duration.ofHours(1),
                endAt = now + Duration.ofHours(1),
                displayOrder = 1,
            ),
            HomeBanner(
                id = EntityIdGenerator.generateRandom(),
                loggingKey = "",
                imageUrl = "",
                clickPageUrl = "",
                clickPageTitle = "",
                startAt = now - Duration.ofHours(1),
                endAt = now + Duration.ofHours(2),
                displayOrder = 4,
            ),
        )
        val inactiveBanners = listOf(
            HomeBanner(
                id = EntityIdGenerator.generateRandom(),
                loggingKey = "",
                imageUrl = "",
                clickPageUrl = "",
                clickPageTitle = "",
                startAt = now - Duration.ofHours(1),
                endAt = now - Duration.ofSeconds(1),
                displayOrder = 2,
            ),
        )

        transactionManager.doInTransaction {
            homeBannerRepository.saveAll(activeBanners + inactiveBanners)
        }

        val user = transactionManager.doInTransaction {
            testDataGenerator.createUser()
        }

        mvc
            .sccRequest("/getHomeBanners", null, user = user)
            .andExpect {
                status { isOk() }
            }
            .apply {
                val result = getResult(GetHomeBannersResponseDto::class)
                assertEquals(activeBanners.sortedBy { it.displayOrder }.map { it.id }, result.banners.map { it.id })
            }
    }
}
