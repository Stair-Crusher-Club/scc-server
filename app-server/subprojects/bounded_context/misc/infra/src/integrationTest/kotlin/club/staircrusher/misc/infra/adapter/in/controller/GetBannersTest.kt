package club.staircrusher.misc.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.GetHomeBannersResponseDto
import club.staircrusher.home_banner.application.port.out.persistence.BannerRepository
import club.staircrusher.home_banner.domain.model.Banner
import club.staircrusher.misc.infra.adapter.`in`.controller.base.MiscITBase
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration

class GetBannersTest : MiscITBase() {
    @Autowired
    private lateinit var bannerRepository: BannerRepository

    @BeforeEach
    fun setUp() {
        transactionManager.doInTransaction {
            bannerRepository.deleteAll()
        }
    }

    @Test
    fun `active 배너가 displayOrder 순으로 내려온다`() {
        val now = SccClock.instant()
        val activeBanners = listOf(
            Banner(
                id = EntityIdGenerator.generateRandom(),
                loggingKey = "aaa",
                imageUrl = "aaa",
                clickPageUrl = "aaa",
                clickPageTitle = "aaa",
                startAt = now - Duration.ofHours(1),
                endAt = null,
                displayOrder = 3,
            ),
            Banner(
                id = EntityIdGenerator.generateRandom(),
                loggingKey = "aaa",
                imageUrl = "aaa",
                clickPageUrl = "aaa",
                clickPageTitle = "aaa",
                startAt = now - Duration.ofHours(1),
                endAt = now + Duration.ofHours(1),
                displayOrder = 1,
            ),
            Banner(
                id = EntityIdGenerator.generateRandom(),
                loggingKey = "aaa",
                imageUrl = "aaa",
                clickPageUrl = "aaa",
                clickPageTitle = "aaa",
                startAt = now - Duration.ofHours(1),
                endAt = now + Duration.ofHours(2),
                displayOrder = 4,
            ),
        )
        val inactiveBanners = listOf(
            Banner(
                id = EntityIdGenerator.generateRandom(),
                loggingKey = "aaa",
                imageUrl = "aaa",
                clickPageUrl = "aaa",
                clickPageTitle = "aaa",
                startAt = now + Duration.ofHours(1),
                endAt = now + Duration.ofHours(2),
                displayOrder = 2,
            ),
        )

        transactionManager.doInTransaction {
            bannerRepository.saveAll(activeBanners + inactiveBanners)
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
