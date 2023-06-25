import { Button, ButtonGroup } from '@blueprintjs/core';
import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AdminApi } from '../../AdminApi';

import './AccessibilityAllowedRegionsPage.scss';
import {AccessibilityAllowedRegionDTO} from "../../api";

function AccessibilityAllowedRegionsPage() {
  const [isLoading, setIsLoading] = useState(false);
  const [accessibilityAllowedRegions, setAccessibilityAllowedRegions] = useState<AccessibilityAllowedRegionDTO[]>([]);
  const navigate = useNavigate();

  function withLoading(promise: Promise<any>): Promise<any> {
    setIsLoading(true);
    return promise.finally(() => setIsLoading(false));
  }

  useEffect(() => {
    withLoading(
      AdminApi.accessibilityAllowedRegionsGet()
        .then(res => setAccessibilityAllowedRegions(res.data) )
    );
  }, []);

  function onAccessibilityAllowedRegionClick(accessibilityAllowedRegion: AccessibilityAllowedRegionDTO) {
    return () => {
      navigate(`/accessibilityAllowedRegions/${accessibilityAllowedRegion.id}`);
    };
  }

  function onAccessibilityAllowedRegionDeleteBtnClick(accessibilityAllowedRegion: AccessibilityAllowedRegionDTO) {
    return async (e: React.MouseEvent) => {
      e.stopPropagation();
      if (!window.confirm(`정말 ${accessibilityAllowedRegion.name} 지역을 삭제하시겠습니까?`)) {
        return;
      }
      await withLoading(
        AdminApi.accessibilityAllowedRegionsRegionIdDelete(accessibilityAllowedRegion.id)
      );
      alert('삭제를 완료했습니다.');

      const res = await AdminApi.accessibilityAllowedRegionsGet()
      setAccessibilityAllowedRegions(res.data);
    };
  }

  return (
    <div>
      <h1>정보 등록 허용 지역</h1>
      <ButtonGroup alignText="right">
        <Link to="/accessibilityAllowedRegion/create">
          <Button text="새 정보 등록 허용 지역 생성" />
        </Link>
      </ButtonGroup>
      <table className="accessibility-allowed-regions bp4-html-table bp4-html-table-bordered bp4-html-table-condensed bp4-interactive">
        <thead>
          <tr>
            <th className="title-column">정보 등록 허용 지역 이름</th>
            <th>삭제</th>
          </tr>
        </thead>
        <tbody>
          {accessibilityAllowedRegions.map((accessibilityAllowedRegion) => {
            return (
              <tr onClick={onAccessibilityAllowedRegionClick(accessibilityAllowedRegion)}>
                <td>{accessibilityAllowedRegion.name}</td>
                <td><Button icon="trash" disabled={isLoading} onClick={onAccessibilityAllowedRegionDeleteBtnClick(accessibilityAllowedRegion)} /></td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  );
}

export default AccessibilityAllowedRegionsPage;
