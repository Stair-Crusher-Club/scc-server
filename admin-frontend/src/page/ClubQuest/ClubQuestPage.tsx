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
  const [clubQuest, setClubQuest] = useState<ClubQuestDTO | null>(null);
  const [currentLocation, setCurrentLocation] = useState<LocationDTO | null>(null);
  const [map, setMap] = useState<any>(null);

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

  function installMap(clubQuest: ClubQuestDTO) {
    if (clubQuest != null && map == null) {
      const container = document.getElementById('map');
      const center = determineCenter(clubQuest.buildings.map(it => it.location));
      const options = {
        center: new window.kakao.maps.LatLng(center.lat, center.lng),
        level: determineLevel(clubQuest.buildings.map(it => it.location)),
      };
      const map = new window.kakao.maps.Map(container, options);
      setMap(map);

      clubQuest.buildings.forEach((target) => {
        const marker = new window.kakao.maps.Marker({
          position: new window.kakao.maps.LatLng(target.location.lat, target.location.lng),
          clickable: true, // ????????? ???????????? ??? ????????? ?????? ???????????? ???????????? ????????? ???????????????.
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
              clickable: true, // ????????? ???????????? ??? ????????? ?????? ???????????? ???????????? ????????? ???????????????.
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
            {clubQuest != null ? <Button text="????????? ?????? ????????????" onClick={showQuestsOnMap}></Button> : null}
            {currentLocation != null ? <Button text="?????? ?????? ????????????" onClick={showCurrentLocationOnMap}></Button> : <Button text="?????? ?????? ???????????? ???..." disabled={true} />}
          </ButtonGroup>
        </div>
        <p className="body-item-fixed-height">
          ??? ?????? ????????? '????????? ??????'??? ????????? ??????????????? ???????????????
        </p>
        <div className="place-list">
          {
            clubQuest
              ? (
                <table className="bp4-html-table bp4-html-table-bordered bp4-html-table-condensed bp4-interactive">
                  <thead>
                    <tr>
                      <th className="title-column">??????</th>
                      <th>?????? ?????? ??????</th>
                      <th>??????</th>
                      <th>??????</th>
                      <th>?????? ??????</th>
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
