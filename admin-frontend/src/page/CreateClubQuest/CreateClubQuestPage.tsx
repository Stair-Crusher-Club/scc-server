import { useState, useEffect } from 'react';
import { Button, NumericInput } from '@blueprintjs/core';

import './CreateClubQuestPage.scss';

declare global {
  interface Window {
    kakao: any;
  }
}

function CreateClubQuestPage() {
  const [isLoading, setIsLoading] = useState(false);
  const [map, setMap] = useState<any>(null);

  function withLoading(promise: Promise<any>): Promise<any> {
    setIsLoading(true);
    return promise.finally(() => setIsLoading(false));
  }

  useEffect(() => {
    installMap();
  }, []);

  function installMap() {
    if (map == null) {
      const container = document.getElementById('map');
      const map = new window.kakao.maps.Map(container, {
        center: new window.kakao.maps.LatLng(37.5642135, 127.0016985), // 서울 중심 좌표
        level: 8,
      });
      setMap(map);
    }
  }

  return (
    <div>
      <h1>퀘스트 생성하기</h1>
      <div className="create-club-quest-page-body">
        <div id="map" className="body-item-fixed-height" />
        <div>
          <span>퀘스트 지역 분할 수 :&nbsp;</span>
          <NumericInput allowNumericCharactersOnly={true}></NumericInput>
          <Button icon="refresh" text="퀘스트 분할하기" onClick={() => {}}></Button>
        </div>
      </div>
    </div>
  );
}

export default CreateClubQuestPage;
