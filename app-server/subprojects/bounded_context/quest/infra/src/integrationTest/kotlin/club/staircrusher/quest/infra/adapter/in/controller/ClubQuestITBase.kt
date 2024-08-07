package club.staircrusher.quest.infra.adapter.`in`.controller

import club.staircrusher.admin_api.spec.dto.ClubQuestCreateDryRunResultItemDTO
import club.staircrusher.admin_api.spec.dto.ClubQuestPurposeTypeEnumDTO
import club.staircrusher.admin_api.spec.dto.ClubQuestTargetBuildingDTO
import club.staircrusher.admin_api.spec.dto.ClubQuestTargetPlaceDTO
import club.staircrusher.admin_api.spec.dto.CreateClubQuestRequest
import club.staircrusher.admin_api.spec.dto.EpochMillisTimestamp
import club.staircrusher.admin_api.spec.dto.LocationDTO
import club.staircrusher.quest.application.port.out.persistence.ClubQuestRepository
import club.staircrusher.quest.application.port.out.persistence.ClubQuestTargetBuildingRepository
import club.staircrusher.quest.application.port.out.persistence.ClubQuestTargetPlaceRepository
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.testing.spring_it.base.SccSpringITBase
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration

open class ClubQuestITBase : SccSpringITBase() {
    @Autowired
    lateinit var clubQuestRepository: ClubQuestRepository

    @Autowired
    lateinit var clubQuestTargetBuildingRepository: ClubQuestTargetBuildingRepository

    @Autowired
    lateinit var clubQuestTargetPlaceRepository: ClubQuestTargetPlaceRepository

    fun getCreateClubQuestRequestBody(
        placeId: String = "placeId"
    ) = CreateClubQuestRequest(
        questNamePrefix = "test",
        purposeType = ClubQuestPurposeTypeEnumDTO.CRUSHER_CLUB,
        startAt = EpochMillisTimestamp(SccClock.instant().toEpochMilli()),
        endAt = EpochMillisTimestamp((SccClock.instant() + Duration.ofDays(7)).toEpochMilli()),
        dryRunResults = listOf(
            ClubQuestCreateDryRunResultItemDTO(
                questNamePostfix = "test",
                questCenterLocation = LocationDTO(lng = 127.0, lat = 37.0),
                targetBuildings = listOf(
                    ClubQuestTargetBuildingDTO(
                        buildingId = "buildingId",
                        name = "buildingName",
                        location = LocationDTO(lng = 127.0, lat = 37.0),
                        places = listOf(
                            ClubQuestTargetPlaceDTO(
                                placeId = placeId,
                                buildingId = "buildingId",
                                name = "placeName",
                                location = LocationDTO(lng = 127.0, lat = 37.0),
                                isConquered = false,
                                isClosedExpected = false,
                                isClosed = false,
                                isNotAccessible = false,
                            ),
                        ),
                    )
                ),
            ),
        ),
    )
}
