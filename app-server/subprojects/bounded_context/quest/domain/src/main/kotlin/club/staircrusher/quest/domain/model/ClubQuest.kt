package club.staircrusher.quest.domain.model

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.env.SccEnv
import club.staircrusher.stdlib.geography.Location
import jakarta.persistence.AttributeOverride
import jakarta.persistence.AttributeOverrides
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import java.time.Instant

@Entity
class ClubQuest(
    @Id
    val id: String,
    val name: String,
    @Enumerated(EnumType.STRING)
    val purposeType: ClubQuestPurposeType,
    val startAt: Instant,
    val endAt: Instant,
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "lng", column = Column(name = "quest_center_location_x")),
        AttributeOverride(name = "lat", column = Column(name = "quest_center_location_y")),
    )
    val questCenterLocation: Location,
    @OneToMany(mappedBy = "clubQuestId", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val targetBuildings: List<ClubQuestTargetBuilding>,
    val createdAt: Instant,
    shortenedAdminUrl: String?,
    updatedAt: Instant,
) {

    var shortenedAdminUrl: String? = shortenedAdminUrl
        protected set

    var updatedAt: Instant = updatedAt
        protected set

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
            purposeType: ClubQuestPurposeType,
            startAt: Instant,
            endAt: Instant,
            dryRunResultItem: ClubQuestCreateDryRunResultItem,
            createdAt: Instant,
        ): ClubQuest {
            val id = EntityIdGenerator.generateRandom()
            require(startAt < endAt) { "퀘스트 종료 시각($endAt)은 퀘스트 시작 시각($startAt) 이후여야 합니다." }
            return ClubQuest(
                id = id,
                name = name,
                purposeType = purposeType,
                startAt = startAt,
                endAt = endAt,
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
