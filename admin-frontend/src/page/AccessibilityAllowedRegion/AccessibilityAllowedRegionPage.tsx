import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {Button, ButtonGroup} from "@blueprintjs/core";
import { AccessibilityAllowedRegionDTO } from '../../api';
import { determineCenter, determineLevel } from '../../util/kakaoMap';
import { AdminApi } from '../../AdminApi';
import LatLng = kakao.maps.LatLng;

import './AccessibilityAllowedRegionPage.scss';

declare global {
  interface Window {
    kakao: any;
  }
}

function AccessibilityAllowedRegionPage() {
  const [isLoading, setIsLoading] = useState(false);
  const [map, setMap] = useState<any>(null);
  const [accessibilityAllowedRegion, setAccessibilityAllowedRegion] = useState<AccessibilityAllowedRegionDTO | null>(null);

  const { id: _rawAccessibilityAllowedRegionId } = useParams();
  const accessibilityAllowedRegionId = _rawAccessibilityAllowedRegionId!

  const navigate = useNavigate()

  function withLoading(promise: Promise<any>): Promise<any> {
    setIsLoading(true);
    return promise.finally(() => setIsLoading(false));
  }

  useEffect(() => {
    withLoading(
      AdminApi.accessibilityAllowedRegionsRegionIdGet(accessibilityAllowedRegionId)
        .then((res) => {
          const accessibilityAllowedRegion = res.data;
          setAccessibilityAllowedRegion(accessibilityAllowedRegion);
          installMap(accessibilityAllowedRegion);
        })
    );
  }, []);

  const installMap = (accessibilityAllowedRegion: AccessibilityAllowedRegionDTO) => {
    if (map == null) {
      const container = document.getElementById('map');
      const center = determineCenter(accessibilityAllowedRegion.boundaryVertices);
      const options = {
        center: new window.kakao.maps.LatLng(center.lat, center.lng),
        level: determineLevel(accessibilityAllowedRegion.boundaryVertices),
      };
      const map = new window.kakao.maps.Map(container, options);
      setMap(map);

      const polygon = new kakao.maps.Polygon({
        path: accessibilityAllowedRegion.boundaryVertices.map(it => (new LatLng(it.lat, it.lng))),
        strokeWeight: 10, // 선의 두께입니다.
        // strokeColor: '#39DE2A', // 선의 색깔입니다.
        strokeOpacity: 1, // 선의 불투명도 입니다. 1에서 0 사이의 값이며 0에 가까울수록 투명합니다.
        // strokeStyle: 'solid', // 선의 스타일입니다.
        // fillColor: '#A2FF99', // 채우기 색깔입니다.
        // fillOpacity: 0.7 // 채우기 불투명도 입니다.
      });
      polygon.setMap(map);
    }
  }

  const onAccessibilityAllowedRegionDeleteBtnClick = async (e: React.MouseEvent) => {
    e.stopPropagation();
    if (!window.confirm(`정말 ${accessibilityAllowedRegion?.name} 지역을 삭제하시겠습니까?`)) {
      return;
    }
    await withLoading(
        AdminApi.accessibilityAllowedRegionsRegionIdDelete(accessibilityAllowedRegion?.id!)
    );
    alert('삭제를 완료했습니다.');
    navigate('/accessibilityAllowedRegions');
  };

  return (
    <div>
      <h1>{accessibilityAllowedRegion?.name}</h1>
      <div className="accessibility-allowed-region-page-body">
        <div id="map" className="body-item-fixed-height" />
        <ButtonGroup>
          <Button icon="trash" text="삭제하기" onClick={onAccessibilityAllowedRegionDeleteBtnClick} disabled={isLoading}></Button>
        </ButtonGroup>
      </div>
    </div>
  );
}

export default AccessibilityAllowedRegionPage;
