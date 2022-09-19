package club.staircrusher.quest.application

import club.staircrusher.quest.domain.entity.ClubQuest
import club.staircrusher.quest.domain.repository.ClubQuestRepository
import club.staircrusher.quest.domain.vo.ClubQuestCreateDryRunResultItem
import club.staircrusher.quest.domain.vo.ClubQuestTargetPlace
import club.staircrusher.stdlib.geography.Location
import java.util.Random
import java.util.UUID

// TODO: 트랜잭션 처리
class ClubQuestCreateAplService(
    private val clubQuestRepository: ClubQuestRepository,
) {
    fun createDryRun(
        centerLocation: Location,
        radiusMeters: Int,
        clusterCount: Int,
    ): List<ClubQuestCreateDryRunResultItem> {
        // TODO: 카카오 지도 API로 장소 긁어오기
        return (0 until 10)
            .map{
                Location(
                    lng = centerLocation.lng + (Random().nextDouble() - 0.5) / 100,
                    lat = centerLocation.lat + (Random().nextDouble() - 0.5) / 100,
                )
            }
            .mapIndexed { questIdx, questCenterLocation ->
                ClubQuestCreateDryRunResultItem(
                    questCenterLocation = questCenterLocation,
                    targetPlaces = (0 until 15).mapIndexed { placeIdx, it ->
                        ClubQuestTargetPlace(
                            name = "퀘스트 $questIdx-$placeIdx",
                            location = Location(
                                lng = questCenterLocation.lng + (Random().nextDouble() - 0.5) / 500,
                                lat = questCenterLocation.lat + (Random().nextDouble() - 0.5) / 500,
                            ),
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
