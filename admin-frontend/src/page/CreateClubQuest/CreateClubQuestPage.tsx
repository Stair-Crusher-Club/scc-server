import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, InputGroup, NumericInput } from '@blueprintjs/core';
import { ClubQuestCreateDryRunResultItemDTO } from '../../api';
import { AdminApi } from '../../AdminApi';

import './CreateClubQuestPage.scss';

declare global {
  interface Window {
    kakao: any;
  }
}

interface QuestCenterIndicator {
  marker: kakao.maps.Marker;
  regionCircle: kakao.maps.Circle;
}

function CreateClubQuestPage() {
  const [isLoading, _setIsLoading] = useState(false);
  const [isDryRunLoading, setIsDryRunLoading] = useState(false);
  const isLoadingRef = useRef(isLoading);
  function setIsLoading(newValue: boolean) {
    isLoadingRef.current = newValue;
    _setIsLoading(newValue);
  }

  const [map, setMap] = useState<kakao.maps.Map | null>(null);

  const [questNamePrefix, setQuestNamePrefix] = useState('');
  const [questCenter, setQuestCenter] = useState<kakao.maps.LatLng | null>(null);
  const [questRadius, setQuestRadius] = useState<number>(200);
  const [questCenterIndicator, setQuestCenterIndicator] = useState<QuestCenterIndicator | null>(null);
  const [questClusterCount, setQuestClusterCount] = useState(1);
  const [maxPlaceCountPerQuest, setMaxPlaceCountPerQuest] = useState(50);

  const [questClustersMarkers, _setQuestClustersMarkers] = useState<kakao.maps.Marker[][]>([]);
  const [dryRunResults, setDryRunResults] = useState<ClubQuestCreateDryRunResultItemDTO[]>([]);
  const [selectedDryRunResultIdx, setSelectedDryRunResultIdx] = useState<number | null>(null);
  const questClustersMarkersRef = useRef(questClustersMarkers);
  function setQuestClustersMarkers(newValue: Array<Array<kakao.maps.Marker>>) {
    questClustersMarkersRef.current = newValue;
    _setQuestClustersMarkers(newValue);
  }

  const navigate = useNavigate();

  function withLoading(promise: Promise<any>): Promise<any> {
    setIsLoading(true);
    return promise.finally(() => setIsLoading(false));
  }

  useEffect(installMapOnce, []);
  useEffect(() => {
    createOrUpdateQuestCenterIndicator();
  }, [questCenter, questRadius]);
  useEffect(() => {
    setQuestClusterCount(Math.ceil((questRadius * questRadius - 1) / (300 * 300))); // 대략 300m x 300m 사이즈의 구역으로 나누는 걸 추천해준다.
  }, [questRadius]);

  function installMapOnce() {
    if (map) {
      return;
    }
    const container = document.getElementById('map');
    const newMap: kakao.maps.Map = new window.kakao.maps.Map(container, {
      center: new window.kakao.maps.LatLng(37.5642135, 127.0016985), // 서울 중심 좌표
      level: 8,
    });
    setMap(newMap);
    window.kakao.maps.event.addListener(newMap, 'click', updateQuestCenterOnMapClick);
    const zoomControl = new window.kakao.maps.ZoomControl();
    newMap.addControl(zoomControl, window.kakao.maps.ControlPosition.RIGHT);
  }

  function updateQuestCenterOnMapClick(mouseEvent: kakao.maps.event.MouseEvent) {
    if (isLoadingRef.current || questClustersMarkersRef.current.length > 0) {
      return; // dryRunCreate를 한 이후에는 지도 클릭 시 퀘스트 중심이 이동하지 않는 것이 좋다.
    }
    setQuestCenter(mouseEvent.latLng);
  }

  function createOrUpdateQuestCenterIndicator() {
    if (questCenterIndicator) {
      questCenterIndicator.marker.setPosition(questCenter!);
      questCenterIndicator.regionCircle.setPosition(questCenter!);
      questCenterIndicator.regionCircle.setRadius(questRadius);
    } else if (questCenter && questRadius) {
      const markerImage = new window.kakao.maps.MarkerImage(
        '/centerLocation.jpg',
        new window.kakao.maps.Size(20, 20),
        { offset: new window.kakao.maps.Point(10, 10) },
      );
      const marker = new window.kakao.maps.Marker({
        position: questCenter,
        image: markerImage,
      });
      marker.setMap(map);
      const regionCircle = new window.kakao.maps.Circle({
        center: questCenter,
        radius: questRadius,
        strokeWeight: 5,
        strokeColor: '#75B8FA',
        strokeOpacity: 0.5,
        fillColor: '#CFE7FF',
        fillOpacity: 0.2,
      });
      regionCircle.setMap(map);
      setQuestCenterIndicator({
        marker,
        regionCircle,
      });
    }
  }

  async function dryRunCreateClubQuest() {
    withLoading((
      async () => {
        setIsDryRunLoading(true);
        
        try {
          const res = await AdminApi.clubQuestsCreateDryRunPost({
            centerLocation: {
              lng: questCenter!.getLng(),
              lat: questCenter!.getLat(),
            },
            radiusMeters: questRadius,
            clusterCount: questClusterCount,
            maxPlaceCountPerQuest: maxPlaceCountPerQuest,
          });
          const dryRunResult = res.data;
          setDryRunResults(dryRunResult);
      
          questClustersMarkers.forEach((questClusterMarkers) => {
            questClusterMarkers.forEach((marker) => {
              marker.setMap(null);
            });
          });
          const newQuestClustersMarkers = dryRunResult.map((item) => {
            return item.targetBuildings.map((targetBuilding) => {
              const marker = new window.kakao.maps.Marker({
                position: new kakao.maps.LatLng(targetBuilding.location.lat, targetBuilding.location.lng),
              });
              marker.setMap(map);
              
              const tooltip = new window.kakao.maps.InfoWindow({
                content : `<div style="padding:5px;">${targetBuilding.name}</div>`,
                removable: true
              });
              window.kakao.maps.event.addListener(marker, 'click', () => {
                // TODO: 툴팁 누르면 스크롤 내려가게 하기; https://www.robinwieruch.de/react-scroll-to-item/
                //       이거 할 때 state 정리도 한 번 하면 좋을 듯. 지금 마커랑 dry run result랑 따로 저장하고 있는데, 적절한 데이터 구조 만들어서 같이 저장?
                tooltip.open(map, marker);
              });

              return marker
            });
          });
          setQuestClustersMarkers(newQuestClustersMarkers);
        } finally {
          setIsDryRunLoading(false);
        }
      }
    )());
  }

  function onClearDryRunResult() {
    setDryRunResults([]);
    hideAllClustersMarkers();
    setQuestClustersMarkers([]);
    setSelectedDryRunResultIdx(null);
  }

  function onShowAllClusters() {
    showAllClustersMarkers();
    setSelectedDryRunResultIdx(null);
  }

  function onShowCluster(selectedIdx: number) {
    return () => {
      showClusterMarkers(selectedIdx);
      setSelectedDryRunResultIdx(selectedIdx);
    }
  }

  function showAllClustersMarkers() {
    questClustersMarkers.forEach((questClusterMarkers) => {
      questClusterMarkers.forEach((marker) => {
        marker.setMap(map);
      });
    });
  }

  function hideAllClustersMarkers() {
    questClustersMarkers.forEach((questClusterMarkers) => {
      questClusterMarkers.forEach((marker) => {
        marker.setMap(null);
      });
    });
  }

  function showClusterMarkers(selectedIdx: number) {
    questClustersMarkers.forEach((questClusterMarkers, idx) => {
      if (idx === selectedIdx) {
        questClusterMarkers.forEach((marker) => {
          marker.setMap(map);
        });
      } else {
        questClusterMarkers.forEach((marker) => {
          marker.setMap(null);
        });
      }
    });
  }

  async function createClubQuest() {
    if (!window.confirm('퀘스트를 생성하시겠습니까?')) {
      return;
    }
    withLoading((
      async () => {
        await AdminApi.clubQuestsCreatePost({
          questNamePrefix,
          dryRunResults,
        });
        alert('퀘스트 생성을 완료했습니다.');
        navigate('/clubQuests');
      }
    )());
  }

  function showingTargetBuildings() {
    if (selectedDryRunResultIdx !== null) {
      return dryRunResults[selectedDryRunResultIdx].targetBuildings;
    }
    return dryRunResults.flatMap(it => it.targetBuildings);
  }

  function totalTargetBuildingsCount() {
    let count = 0;
    dryRunResults.forEach((dryRunResult, idx) => {
      dryRunResult.targetBuildings.forEach(() => {
        if (selectedDryRunResultIdx == null || selectedDryRunResultIdx === idx) {
          count += 1;
        }
      });
    });
    return count;
  }

  function totalTargetPlacesCount() {
    let count = 0;
    dryRunResults.forEach((dryRunResult, idx) => {
      dryRunResult.targetBuildings.forEach((targetBuilding) => {
        targetBuilding.places.forEach(() => {
          if (selectedDryRunResultIdx == null || selectedDryRunResultIdx === idx) {
            count += 1;
          }
        });
      });
    });
    return count;
  }

  return (
    <div>
      <h1>퀘스트 생성하기</h1>
      <div className="create-club-quest-page-body">
        <div id="map" className="body-item-fixed-height" />
        <div>
          <div className="input-group">
            <span>퀘스트 이름 :&nbsp;</span>
            <InputGroup
              className="inline-flex"
              value={questNamePrefix}
              onChange={(event) => { setQuestNamePrefix(event.target.value); }}
              disabled={isLoading}
            />
          </div>
          <div className="input-group">
            <span>퀘스트 지역 반경(m) :&nbsp;</span>
            <NumericInput
              className="inline-flex"
              allowNumericCharactersOnly={true}
              majorStepSize={100}
              stepSize={100}
              min={100}
              max={3000}
              value={questRadius}
              onValueChange={setQuestRadius}
              disabled={isLoading}
            />
          </div>
          <div className="input-group">
            <span>퀘스트 지역 분할 수 :&nbsp;</span>
            <NumericInput
              className="inline-flex"
              allowNumericCharactersOnly={true}
              min={1}
              max={200}
              value={questClusterCount}
              onValueChange={setQuestClusterCount}
              disabled={isLoading}
            />
          </div>
          <div className="input-group">
            <span>퀘스트 당 최대 장소 수 :&nbsp;</span>
            <NumericInput
                className="inline-flex"
                allowNumericCharactersOnly={true}
                min={1}
                value={maxPlaceCountPerQuest}
                onValueChange={setMaxPlaceCountPerQuest}
                disabled={isLoading}
            />
          </div>
        </div>
        <div className="input-group">
          <Button icon="refresh" text="장소 조회 & 분할하기" onClick={dryRunCreateClubQuest} disabled={isLoading || !questCenter || !questRadius || !questClusterCount || !maxPlaceCountPerQuest}></Button>
          <Button icon="confirm" text="확정하기 (퀘스트 생성)" onClick={createClubQuest} disabled={isLoading || dryRunResults.length === 0}></Button>
          <Button icon="trash" text="처음부터 다시하기" onClick={onClearDryRunResult} disabled={isLoading || dryRunResults.length === 0}></Button>
        </div>
        {isDryRunLoading ? <p>장소를 조회하는 중입니다. 최대 수 분 정도 걸릴 수 있으니 페이지를 종료하지 마시고 기다려 주세요!</p> : null}
        {
          dryRunResults.length > 0
            ? (
              <div>
                <p>※ 퀘스트 중심 위치를 변경하려면 [처음부터 다시하기] 버튼을 눌러주세요.</p>
                <p>분할 결과 : 건물 {totalTargetBuildingsCount()}개 / 장소 {totalTargetPlacesCount()}개</p>
                <div className="dry-run-result-container">
                  <div className="dry-run-result-sidebar">
                    <Button className="cluster-button" text="전체 표시" onClick={onShowAllClusters} disabled={isLoading} />
                    {
                      dryRunResults.map((dryRunResult, idx) => (
                        <Button className="cluster-button" key={idx} text={dryRunResult.questNamePostfix} onClick={onShowCluster(idx)} disabled={isLoading} />
                      ))
                    }
                  </div>
                  <div className="dry-run-result-body">
                    <table className="bp4-html-table bp4-html-table-bordered bp4-html-table-condensed bp4-interactive">
                      <thead>
                        <tr>
                          <th className="title-column">건물</th>
                          <th>점포 또는 매장</th>
                        </tr>
                      </thead>
                      <tbody>
                        {
                          showingTargetBuildings().flatMap((building) => {
                            return building.places.map((place, idx) => {
                              return (
                                <tr key={place.placeId}>
                                  <td>{idx === 0 ? building.name : '상동'}</td>
                                  <td>{place.name}</td>
                                </tr>
                              );
                            });
                          })
                        }
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            )
            : null
        }
      </div>
    </div>
  );
}

export default CreateClubQuestPage;
