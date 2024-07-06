package club.staircrusher.quest.domain.model

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.env.SccEnv
import club.staircrusher.stdlib.geography.Location
import java.time.Instant

class ClubQuest(
    val id: String,
    val name: String,
    val questCenterLocation: Location,
    targetBuildings: List<ClubQuestTargetBuilding>,
    val createdAt: Instant,
    shortenedAdminUrl: String?,
    updatedAt: Instant,
) {
    var targetBuildings: List<ClubQuestTargetBuilding> = targetBuildings
        private set

    var shortenedAdminUrl: String? = shortenedAdminUrl
        private set

    var updatedAt: Instant = updatedAt
        private set

    val originalAdminUrl: String
        get() {
            val baseUrl = when (SccEnv.getEnv()) {
                SccEnv.TEST,
                SccEnv.LOCAL -> "https://localhost:3066"
                SccEnv.DEV -> "https://admin.dev.staircrusher.club"
                SccEnv.PROD -> "https://admin.staircrusher.club"
            }
            return "$baseUrl/public/quest/${id}"
        }

    fun updateShortenedAdminUrl(shortenedAdminUrl: String) {
        this.shortenedAdminUrl = shortenedAdminUrl
        updatedAt = SccClock.instant()
    }

    override fun equals(other: Any?): Boolean {
        return other is ClubQuest && other.id == id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object {
        fun of(
            name: String,
            dryRunResultItem: ClubQuestCreateDryRunResultItem,
            createdAt: Instant,
        ): ClubQuest {
            val id = EntityIdGenerator.generateRandom()
            return ClubQuest(
                id = id,
                name = name,
                questCenterLocation = dryRunResultItem.questCenterLocation,
                targetBuildings = dryRunResultItem.targetBuildings.map {
                    ClubQuestTargetBuilding.of(valueObject = it, clubQuestId = id)
                },
                shortenedAdminUrl = null,
                createdAt = createdAt,
                updatedAt = createdAt,
            )
        }
    }
}
