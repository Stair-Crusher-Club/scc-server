import { useState, useEffect, useRef } from 'react';
import { useParams } from 'react-router-dom';
import { Button, ButtonGroup, Checkbox } from '@blueprintjs/core';
import { LocationDTO } from '../../type';
import { ClubQuestTargetPlaceDTO, ClubQuestDTO, ClubQuestTargetBuildingDTO } from '../../api';
import { determineCenter, determineLevel } from '../../util/kakaoMap';
import { AdminApi } from '../../AdminApi';

import './ClubQuestPage.scss';

declare global {
  interface Window {
    kakao: any;
  }
}

interface BuildingUI {
  id: string;
  marker: kakao.maps.Marker;
  tooltip: kakao.maps.InfoWindow;
}

function ClubQuestPage() {
  const [isLoading, setIsLoading] = useState(false);
  const [map, _setMap] = useState<kakao.maps.Map | null>(null);
  const mapRef = useRef(map)
  function setMap(newValue: kakao.maps.Map) {
    _setMap(newValue);
    mapRef.current = newValue;
  }
  const [clubQuest, _setClubQuest] = useState<ClubQuestDTO | null>(null);
  function setClubQuest(newValue: ClubQuestDTO) {
    _setClubQuest(newValue)
    if (mapRef.current) {
      refreshBuildingMarkers(mapRef.current, newValue)
    }
  }
  const [currentLocation, setCurrentLocation] = useState<LocationDTO | null>(null);
  const [buildingUIs, _setBuildingUIs] = useState<BuildingUI[]>([]);
  const buildingUIsRef = useRef(buildingUIs);
  function setBuildingUIs(newValue: BuildingUI[]) {
    _setBuildingUIs(newValue);
    buildingUIsRef.current = newValue;
  }
  const [focusedBuildingUI, _setFocusedBuildingUI] = useState<BuildingUI | null>(null);
  const focusedBuildingUIRef = useRef(focusedBuildingUI);
  function setFocusedBuildingUI(newValue: BuildingUI | null) {
    _setFocusedBuildingUI(newValue);
    focusedBuildingUIRef.current = newValue;
  }

  const { id: _rawClubQuestId } = useParams();
  const clubQuestId = _rawClubQuestId!

  function withLoading(promise: Promise<any>): Promise<any> {
    setIsLoading(true);
    return promise.finally(() => setIsLoading(false));
  }

  useEffect(() => {
    withLoading(
      AdminApi.clubQuestsClubQuestIdGet(clubQuestId)
        .then((res) => {
          const clubQuest = res.data;
          setClubQuest(clubQuest);
          installMap(clubQuest);
        })
    );
  }, []);
  useEffect(() => {
    const timeout = setTimeout(() => {
      withLoading(
        AdminApi.clubQuestsClubQuestIdGet(clubQuestId)
          .then((res) => {
            const clubQuest = res.data;
            setClubQuest(clubQuest);
          })
      );
    }, 5000);
    return () => { clearTimeout(timeout); };
  }, [clubQuest]);

  const refreshBuildingMarkers = (map: any, clubQuest: ClubQuestDTO) => {
    buildingUIsRef.current.forEach(it => it.marker.setMap(null));

    const newBuildingUIs: BuildingUI[] = [];
    let currentTooltipMarker: kakao.maps.Marker | null = null;
    clubQuest.buildings.forEach((target) => {
      const isAllPlaceConquered = target.places.every(it => it.isConquered || it.isClosed || it.isNotAccessible)
      const finishedQuestMarkerImage = new window.kakao.maps.MarkerImage(
          '/finishedQuestMarker.png',
          new window.kakao.maps.Size(24, 36),
      );
      const marker: kakao.maps.Marker = new window.kakao.maps.Marker({
        position: new window.kakao.maps.LatLng(target.location.lat, target.location.lng),
        image: isAllPlaceConquered ? finishedQuestMarkerImage : null,
        clickable: true, // 마커를 클릭했을 때 지도의 클릭 이벤트가 발생하지 않도록 설정합니다.
      });
      marker.setMap(map);
      window.kakao.maps.event.addListener(marker, 'click', () => {
        handleBuildingClick(target);
      });

      let tooltip: kakao.maps.InfoWindow = new window.kakao.maps.InfoWindow({
        content: `<div style="padding:5px;">${getTooltipDisplayedName(target)}</div>`,
        removable: true
      });
      if (tooltip.getContent() === focusedBuildingUIRef.current?.tooltip.getContent()) {
        tooltip = focusedBuildingUIRef.current!.tooltip;
        currentTooltipMarker = marker;
      }

      newBuildingUIs.push({
        id: target.buildingId,
        marker,
        tooltip,
      });
    });

    setBuildingUIs(newBuildingUIs);

    // 마커를 다시 그렸으므로, UI상 툴팁이 가장 뒤로 밀려난 상태이다.
    // 따라서 툴팁을 맨 위로 올려주기 위해 한 번 숨겼다가 다시 그려준다.
    focusedBuildingUIRef.current?.tooltip?.close();
    focusedBuildingUIRef.current?.tooltip?.open(map, currentTooltipMarker!);
  }

  const getTooltipDisplayedName = (targetBuilding: ClubQuestTargetBuildingDTO): string => {
    const placeName = targetBuilding.places[0].name;
    let shortenedPlaceName = '';
    if (placeName.length <= 7) {
      shortenedPlaceName = placeName;
    } else {
      shortenedPlaceName = `${placeName.substring(0, 6)}...`;
    }
    return `${targetBuilding.name}: ${shortenedPlaceName}`;
  }

  const installMap = (clubQuest: ClubQuestDTO) => {
    if (mapRef.current == null) {
      const container = document.getElementById('map');
      const center = determineCenter(clubQuest.buildings.map(it => it.location));
      const options = {
        center: new window.kakao.maps.LatLng(center.lat, center.lng),
        level: determineLevel(clubQuest.buildings.map(it => it.location)),
      };
      const map = new window.kakao.maps.Map(container, options);
      setMap(map);
      refreshBuildingMarkers(map, clubQuest)

      if (navigator.geolocation != null) {
        let currentLocationMarker: any = null;
        const updateCurrentLocationMarker = () => {
          navigator.geolocation.getCurrentPosition((position) => {
            setCurrentLocation({ lat: position.coords.latitude, lng: position.coords.longitude });

            if (currentLocationMarker != null) {
              currentLocationMarker.setMap(null);
            }

            const markerImage = new window.kakao.maps.MarkerImage(
              '/currentLocation.png',
              new window.kakao.maps.Size(20, 20),
              { offset: new window.kakao.maps.Point(10, 10) },
            );
            currentLocationMarker = new window.kakao.maps.Marker({
              position: new window.kakao.maps.LatLng(position.coords.latitude, position.coords.longitude),
              image: markerImage,
              clickable: true, // 마커를 클릭했을 때 지도의 클릭 이벤트가 발생하지 않도록 설정합니다.
            });
            currentLocationMarker.setMap(map);
          });
        }

        updateCurrentLocationMarker();
        setInterval(updateCurrentLocationMarker, 5000);
      }
    }
  }

  const unfocusBuildingUI = () => {
    focusedBuildingUI?.tooltip?.close();
    setFocusedBuildingUI(null);
  }
  const showQuestsOnMap = () => {
    if (clubQuest != null) {
      const center = determineCenter(clubQuest.buildings.map(it => it.location ));
      mapRef.current!.setLevel(determineLevel(clubQuest.buildings.map(it => it.location)));
      mapRef.current!.panTo(new window.kakao.maps.LatLng(center.lat, center.lng));
    }
  };
  const showCurrentLocationOnMap = () => {
    if (clubQuest != null && currentLocation != null) {
      mapRef.current!.panTo(new window.kakao.maps.LatLng(currentLocation?.lat, currentLocation?.lng));
    }
  }

  const onPlaceIsClosedChange = (place: ClubQuestTargetPlaceDTO) => {
    return (event: React.FormEvent<HTMLElement>) => {
      withLoading(
        AdminApi.clubQuestsClubQuestIdIsClosedPut(clubQuest!.id, {
          buildingId: place.buildingId,
          placeId: place.placeId,
          isClosed: (event.target as HTMLInputElement).checked,
        })
          .then((res) => setClubQuest(res.data))
      );
    };
  }

  const onPlaceIsNotAccessibleChange = (place: ClubQuestTargetPlaceDTO) => {
    return (event: React.FormEvent<HTMLElement>) => {
      withLoading(
        AdminApi.clubQuestsClubQuestIdIsNotAccessiblePut(clubQuest!.id, {
          buildingId: place.buildingId,
          placeId: place.placeId,
          isNotAccessible: (event.target as HTMLInputElement).checked,
        })
          .then((res) => setClubQuest(res.data))
      );
    };
  }

  const handleBuildingClick = (building: ClubQuestTargetBuildingDTO) => {
    const buildingUI = buildingUIsRef.current.find(it => it.id === building.buildingId)!;
    focusedBuildingUIRef?.current?.tooltip?.close();
    buildingUI.tooltip.open(mapRef.current!, buildingUI.marker);
    setFocusedBuildingUI(buildingUI);

    const latLng = new window.kakao.maps.LatLng(building.location.lat, building.location.lng);
    mapRef.current!.panTo(latLng);
  }

  return (
    <div>
      <h1>{clubQuest?.name}</h1>
      <div className="club-quest-page-body">
        <p className="body-item-fixed-height">
          ※ 폐업여부는 ‘네이버지도'로 확인하면 더 정확합니다.
          <br />
          ※ 파란색 마커: 정복할 장소가 남아 있음
          <br />
          ※ 회색 마커: 정복/폐업/접근불가 중 하나라도 체크 된 경우
          <br />
          ※ 앱에서 정보 완료시, 정복란이 자동으로 체크됨
          <br />
          ※ 마커 클릭시, 건물번호와 대표 장소가 노출됨
        </p>
        <div id="map" className="body-item-fixed-height" />
        <div className="map-manipulate-button-div body-item-fixed-height">
          <ButtonGroup className="map-manipulate-button-container">
            {clubQuest != null ? <Button text="전체 장소 목록 표시하기" disabled={focusedBuildingUI == null} onClick={unfocusBuildingUI}></Button> : null}
            {clubQuest != null ? <Button text="퀘스트 전체 표시하기" onClick={showQuestsOnMap}></Button> : null}
            {currentLocation != null ? <Button text="현재 위치 표시하기" onClick={showCurrentLocationOnMap}></Button> : <Button text="현재 위치 가져오는 중..." disabled={true} />}
          </ButtonGroup>
        </div>
        <div className="place-list">
          {
            clubQuest
              ? (
                <table className="bp4-html-table bp4-html-table-bordered bp4-html-table-condensed bp4-interactive">
                  <thead>
                    <tr>
                      <th className="title-column">건물</th>
                      <th>점포 또는 매장</th>
                      <th>정복</th>
                      <th>폐업</th>
                      <th>접근 불가</th>
                    </tr>
                  </thead>
                  <tbody>
                    {
                      clubQuest.buildings.filter(it => focusedBuildingUI == null || it.buildingId === focusedBuildingUI.id).flatMap((building) => {
                        return building.places.map((place, idx) => {
                          return (
                            <tr onClick={() => handleBuildingClick(building)}>
                              <td>{idx === 0 ? building.name : ''}</td>
                              <td>{place.name}</td>
                              <td><Checkbox checked={place.isConquered} disabled={true} large /></td>
                              <td><Checkbox checked={place.isClosed} disabled={isLoading} large onChange={onPlaceIsClosedChange(place)} /></td>
                              <td><Checkbox checked={place.isNotAccessible} disabled={isLoading} large onChange={onPlaceIsNotAccessibleChange(place)} /></td>
                            </tr>
                          );
                        });
                      })
                    }
                  </tbody>
                </table>
              )
              : null
          }
        </div>
      </div>
    </div>
  );
}

export default ClubQuestPage;
