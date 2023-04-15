import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { Button, ButtonGroup, Checkbox } from '@blueprintjs/core';
import { LocationDTO } from '../../type';
import { ClubQuestTargetPlaceDTO, ClubQuestDTO } from '../../api';
import { determineCenter, determineLevel } from '../../util/kakaoMap';
import { AdminApi } from '../../AdminApi';

import './ClubQuestPage.scss';

declare global {
  interface Window {
    kakao: any;
  }
}

function ClubQuestPage() {
  const [isLoading, setIsLoading] = useState(false);
  const [clubQuest, _setClubQuest] = useState<ClubQuestDTO | null>(null);
  const [currentLocation, setCurrentLocation] = useState<LocationDTO | null>(null);
  const [map, setMap] = useState<any>(null);
  function setClubQuest(clubQuest: ClubQuestDTO) {
    _setClubQuest(clubQuest)
    refreshBuildingMarkers(map, clubQuest)
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
    clubQuest.buildings.forEach((target) => {
      const isAllPlaceConquered = target.places.every(it => it.isConquered || it.isClosed || it.isNotAccessible)
      const finishedQuestMarkerImage = new window.kakao.maps.MarkerImage(
          '/finishedQuestMarker.png',
          new window.kakao.maps.Size(24, 36),
      );
      const marker = new window.kakao.maps.Marker({
        position: new window.kakao.maps.LatLng(target.location.lat, target.location.lng),
        image: isAllPlaceConquered ? finishedQuestMarkerImage : null,
        clickable: true, // 마커를 클릭했을 때 지도의 클릭 이벤트가 발생하지 않도록 설정합니다.
      });
      marker.setMap(map);

      const tooltip = new window.kakao.maps.InfoWindow({
        content : `<div style="padding:5px;">${target.name}</div>`,
        removable: true
      });
      window.kakao.maps.event.addListener(marker, 'click', () => {
        tooltip.open(map, marker);
      });
    });
  }

  const installMap = (clubQuest: ClubQuestDTO) => {
    if (clubQuest != null && map == null) {
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

  const showQuestsOnMap = () => {
    if (clubQuest != null) {
      const center = determineCenter(clubQuest.buildings.map(it => it.location ));
      map.setLevel(determineLevel(clubQuest.buildings.map(it => it.location)));
      map.panTo(new window.kakao.maps.LatLng(center.lat, center.lng));
    }
  };
  const showCurrentLocationOnMap = () => {
    if (clubQuest != null && currentLocation != null) {
      map.panTo(new window.kakao.maps.LatLng(currentLocation?.lat, currentLocation?.lng));
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

  return (
    <div>
      <h1>{clubQuest?.name}</h1>
      <div className="club-quest-page-body">
        <div id="map" className="body-item-fixed-height" />
        <div className="map-manipulate-button-div body-item-fixed-height">
          <ButtonGroup className="map-manipulate-button-container">
            {clubQuest != null ? <Button text="퀘스트 전체 표시하기" onClick={showQuestsOnMap}></Button> : null}
            {currentLocation != null ? <Button text="현재 위치 표시하기" onClick={showCurrentLocationOnMap}></Button> : <Button text="현재 위치 가져오는 중..." disabled={true} />}
          </ButtonGroup>
        </div>
        <p className="body-item-fixed-height">
          ※ 폐업 여부는 '네이버 지도'로 검색해 확인하시면 편리합니다
        </p>
        <p className="body-item-fixed-height">
          ※ 파란색 마커는 아직 정복할 장소가 남아 있는 건물이고, 회색 마커는 건물 안의 모든 장소가 정복 or 폐업 or 접근 불가인 건물입니다.
        </p>
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
                      clubQuest.buildings.flatMap((building) => {
                        return building.places.map((place, idx) => {
                          return (
                            <tr>
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
