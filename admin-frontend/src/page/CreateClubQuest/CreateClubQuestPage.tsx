import { useState, useEffect, useRef } from 'react';
import { Button, NumericInput } from '@blueprintjs/core';
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
  const [isLoading, setIsLoading] = useState(false);
  const [map, setMap] = useState<kakao.maps.Map | null>(null);

  const [questCenter, setQuestCenter] = useState<kakao.maps.LatLng | null>(null);
  const [questRadius, setQuestRadius] = useState<number>(200);
  const [questCenterIndicator, setQuestCenterIndicator] = useState<QuestCenterIndicator | null>(null);
  const [questClusterCount, setQuestClusterCount] = useState(1);

  const [questClustersMarkers, _setQuestClustersMarkers] = useState<kakao.maps.Marker[][]>([]);
  const [dryRunResults, setDryRunResults] = useState<ClubQuestCreateDryRunResultItemDTO[]>([]);
  const questClustersMarkersRef = useRef(questClustersMarkers);
  function setQuestClustersMarkers(newValue: Array<Array<kakao.maps.Marker>>) {
    questClustersMarkersRef.current = newValue;
    _setQuestClustersMarkers(newValue);
  }

  function withLoading(promise: Promise<any>): Promise<any> {
    setIsLoading(true);
    return promise.finally(() => setIsLoading(false));
  }

  useEffect(installMapOnce, []);
  useEffect(() => {
    createOrUpdateQuestCenterIndicator();
    setQuestClusterCount(Math.ceil((questRadius * questRadius - 1) / (300 * 300))); // 대략 300m x 300m 사이즈의 구역으로 나누는 걸 추천해준다.
  }, [questCenter, questRadius]);

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
    if (questClustersMarkersRef.current.length > 0) {
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
      const marker = new window.kakao.maps.Marker({
        position: questCenter,
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
    const res = await AdminApi.clubQuestsCreateDryRunPost({
      centerLocation: {
        lng: questCenter!.getLng(),
        lat: questCenter!.getLat(),
      },
      radiusMeters: questRadius,
      clusterCount: questClusterCount,
    });
    const dryRunResult = res.data;
    setDryRunResults(dryRunResult)

    questClustersMarkers.forEach((questClusterMarkers) => {
      questClusterMarkers.forEach((marker) => {
        marker.setMap(null);
      });
    });
    const newQuestClustersMarkers = dryRunResult.map((targetPlaces) => {
      return targetPlaces.targetPlaces.map((targetPlace) => {
        const marker = new window.kakao.maps.Marker({
          position: new kakao.maps.LatLng(targetPlace.location.lat, targetPlace.location.lng),
        });
        marker.setMap(map);
        return marker
      });
    });
    setQuestClustersMarkers(newQuestClustersMarkers);
  }

  function clearDryRunResult() {
    setDryRunResults([]);
    questClustersMarkers.forEach((questClusterMarkers, idx) => {
      questClusterMarkers.forEach((marker) => {
        marker.setMap(null);
      });
    });
    setQuestClustersMarkers([]);
  }

  function showAllClustersMarkers() {
    questClustersMarkers.forEach((questClusterMarkers, idx) => {
      questClusterMarkers.forEach((marker) => {
        marker.setMap(map);
      });
    });
  }

  function showOnlyClusterMarkers(selectedIdx: number) {
    return () => {
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
    };
  }

  async function createClubQuest() {
    if (!window.confirm('퀘스트를 생성하시겠습니까?')) {
      return;
    }
    await AdminApi.clubQuestsCreatePost({
      questNamePrefix: 'haha',
      dryRunResults,
    });
    alert('퀘스트 생성을 완료했습니다.');
    // TODO: 퀘스트 상세 페이지 혹은 퀘스트 목록 페이지로 이동
  }

  return (
    <div>
      <h1>퀘스트 생성하기</h1>
      <div className="create-club-quest-page-body">
        <div id="map" className="body-item-fixed-height" />
        <div>
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
            />
          </div>
          <Button icon="refresh" text="퀘스트 분할하기" onClick={dryRunCreateClubQuest} disabled={!questCenter || !questRadius || !questClusterCount}></Button>
          <Button icon="trash" text="처음부터 다시하기" onClick={clearDryRunResult} disabled={dryRunResults.length === 0}></Button>
          <Button icon="confirm" text="확정하기 (퀘스트 생성)" onClick={createClubQuest} disabled={dryRunResults.length === 0}></Button>
        </div>
        {
          questClustersMarkers.length > 0
            ? (
              <div>
                <p>분할 결과 :</p>
                <Button text="전체 표시" onClick={showAllClustersMarkers} />
                {
                  questClustersMarkers.map((_, idx) => (
                    <Button key={idx} text={`클러스터 ${idx + 1}`} onClick={showOnlyClusterMarkers(idx)} />
                  ))
                }
              </div>
            )
            : null
        }
      </div>
    </div>
  );
}

export default CreateClubQuestPage;