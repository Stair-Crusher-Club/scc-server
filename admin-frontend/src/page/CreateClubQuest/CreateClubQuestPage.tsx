import { useState, useEffect } from 'react';
import { Button, NumericInput } from '@blueprintjs/core';

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
  const [map, setMap] = useState<any>(null);

  const [questCenter, setQuestCenter] = useState<kakao.maps.LatLng | null>(null);
  const [questRadius, setQuestRadius] = useState<number>(200);
  const [questCenterIndicator, setQuestCenterIndicator] = useState<QuestCenterIndicator | null>(null);

  const [questClusterCount, setQuestClusterCount] = useState(1);

  function withLoading(promise: Promise<any>): Promise<any> {
    setIsLoading(true);
    return promise.finally(() => setIsLoading(false));
  }

  useEffect(installMapOnce, []);
  useEffect(() => {
    createOrUpdateQuestCenterIndicator();
    setQuestClusterCount(Math.ceil((questRadius * questRadius - 1) / (200 * 200)));
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
    window.kakao.maps.event.addListener(newMap, 'click', (mouseEvent: kakao.maps.event.MouseEvent) => {
      setQuestCenter(mouseEvent.latLng);
    });
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
    // 대충 200m * 200m 짜리를 한 구역에 적절한 넓이라고 생각하고 퀘스트 분할 수를 추천해준다.
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
          <Button icon="refresh" text="퀘스트 분할하기" onClick={() => {}}></Button>
        </div>
      </div>
    </div>
  );
}

export default CreateClubQuestPage;
