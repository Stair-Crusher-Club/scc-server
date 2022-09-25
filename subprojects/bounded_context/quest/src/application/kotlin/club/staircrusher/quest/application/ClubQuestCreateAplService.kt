package club.staircrusher.quest.application

import club.staircrusher.quest.domain.entity.ClubQuest
import club.staircrusher.quest.domain.repository.ClubQuestRepository
import club.staircrusher.quest.domain.service.PlaceClusterer
import club.staircrusher.quest.domain.vo.ClubQuestCreateDryRunResultItem
import club.staircrusher.quest.domain.vo.ClubQuestTargetPlace
import club.staircrusher.stdlib.geography.Location
import org.springframework.stereotype.Component
import java.util.Random
import java.util.UUID

// TODO: 트랜잭션 처리
@Component
class ClubQuestCreateAplService(
    private val clubQuestRepository: ClubQuestRepository,
    private val placeClusterer: PlaceClusterer,
) {
    fun createDryRun(
        centerLocation: Location,
        radiusMeters: Int,
        clusterCount: Int,
    ): List<ClubQuestCreateDryRunResultItem> {
        // TODO: 카카오 지도 API로 장소 긁어오기
        val randomLocations = (0 until 150)
            .map {
                Location(
                    lng = centerLocation.lng + (Random().nextDouble() - 0.5) / 100,
                    lat = centerLocation.lat + (Random().nextDouble() - 0.5) / 100,
                )
            }
        return placeClusterer.clusterPlaces(randomLocations, clusterCount)
            .toList()
            .mapIndexed { questIdx, (questCenterLocation, locations) ->
                ClubQuestCreateDryRunResultItem(
                    questCenterLocation = questCenterLocation,
                    targetPlaces = locations.mapIndexed { placeIdx, location ->
                        ClubQuestTargetPlace(
                            name = "퀘스트 $questIdx-$placeIdx",
                            location = location,
                            placeId = "$questIdx-$placeIdx",
                        )
                    }
                )
            }
    }

    fun createFromDryRunResult(
        questNamePrefix: String,
        dryRunResultItems: List<ClubQuestCreateDryRunResultItem>
    ) {
        dryRunResultItems.forEachIndexed { idx, dryRunResultItem ->
            clubQuestRepository.save(ClubQuest(
                id = UUID.randomUUID().toString(), // TODO: entity id 생성 전략 수립
                name = "$questNamePrefix $idx",
                dryRunResultItem = dryRunResultItem,
            ))
        }
    }
}
