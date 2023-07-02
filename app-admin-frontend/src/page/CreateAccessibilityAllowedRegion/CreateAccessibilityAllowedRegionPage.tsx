import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, InputGroup } from '@blueprintjs/core';
import { AdminApi } from '../../AdminApi';

import './CreateAccessibilityAllowedRegionPage.scss';

declare global {
  interface Window {
    kakao: any;
  }
}

function CreateAccessibilityAllowedRegionPage() {
  const [isLoading, _setIsLoading] = useState(false);
  const isLoadingRef = useRef(isLoading);
  function setIsLoading(newValue: boolean) {
    isLoadingRef.current = newValue;
    _setIsLoading(newValue);
  }

  const [map, setMap] = useState<kakao.maps.Map | null>(null);
  const [regionName, setRegionName] = useState('');
  const [boundaryVertices, _setBoundaryVertices] = useState<kakao.maps.LatLng[]>([]);
  const polygonVerticesRef = useRef(boundaryVertices)
  function setPolygonVertices(value: kakao.maps.LatLng[]) {
    polygonVerticesRef.current = value;
    _setBoundaryVertices(value);
  }
  useEffect(createOrUpdatePolygon, [boundaryVertices]);
  const [polygon, setPolygon] = useState<kakao.maps.Polygon | null>(null);

  const navigate = useNavigate();

  function withLoading(promise: Promise<any>): Promise<any> {
    setIsLoading(true);
    return promise.finally(() => setIsLoading(false));
  }

  useEffect(installMapOnce, []);

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
    window.kakao.maps.event.addListener(newMap, 'click', addPolygonVertex);
    const zoomControl = new window.kakao.maps.ZoomControl();
    newMap.addControl(zoomControl, window.kakao.maps.ControlPosition.RIGHT);
  }

  function addPolygonVertex(mouseEvent: kakao.maps.event.MouseEvent) {
    const latLng = mouseEvent.latLng;
    setPolygonVertices([...polygonVerticesRef.current, latLng]);
  }

  function createOrUpdatePolygon() {
    if (polygon != null) {
      polygon.setMap(null);
    }

    const newPolygon = new kakao.maps.Polygon({
      path: polygonVerticesRef.current,
      strokeWeight: 10, // 선의 두께입니다.
      // strokeColor: '#39DE2A', // 선의 색깔입니다.
      strokeOpacity: 1, // 선의 불투명도 입니다. 1에서 0 사이의 값이며 0에 가까울수록 투명합니다.
      // strokeStyle: 'solid', // 선의 스타일입니다.
      // fillColor: '#A2FF99', // 채우기 색깔입니다.
      // fillOpacity: 0.7 // 채우기 불투명도 입니다.
    });
    newPolygon.setMap(map);
    setPolygon(newPolygon);
  }

  function onDeleteLastVertex() {
    setPolygonVertices(polygonVerticesRef.current.slice(0, -1));
  }

  function onDeleteAllVertices() {
    setPolygonVertices([]);
  }

  async function createAccessibilityAllowedRegion() {
    if (!window.confirm('정보 등록 허용 지역을 생성하시겠습니까?')) {
      return;
    }
    withLoading((
      async () => {
        await AdminApi.accessibilityAllowedRegionsPost({
          name: regionName,
          boundaryVertices: boundaryVertices.map(it => ({ lng: it.getLng(), lat: it.getLat() })),
        });
        alert('정보 등록 허용 지역 생성을 완료했습니다.');
        navigate('/accessibilityAllowedRegions');
      }
    )());
  }

  return (
    <div>
      <h1>정보 등록 허용 지역 생성하기</h1>
      <div className="create-accessibility-allowed-region-page-body">
        <div id="map" className="body-item-fixed-height" />
        <div>
          <div className="input-group">
            <span>지역 이름 :&nbsp;</span>
            <InputGroup
              className="inline-flex"
              value={regionName}
              onChange={(event) => { setRegionName(event.target.value); }}
              disabled={isLoading}
            />
          </div>
          <Button icon="confirm" text="확정하기 (정보 등록 허용 지역 생성)" onClick={createAccessibilityAllowedRegion} disabled={isLoading || boundaryVertices.length == 0 || !regionName}></Button>
          <Button icon="trash" text="마지막 점 없애기" onClick={onDeleteLastVertex} disabled={isLoading || boundaryVertices.length == 0}></Button>
          <Button icon="trash" text="모든 점 없애기" onClick={onDeleteAllVertices} disabled={isLoading || boundaryVertices.length == 0}></Button>
        </div>
      </div>
    </div>
  );
}

export default CreateAccessibilityAllowedRegionPage;
