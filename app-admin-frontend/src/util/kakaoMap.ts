import haversine from 'haversine';
import { LocationDTO } from "../type";
import { max, min } from './math';

export function determineCenter(locations: LocationDTO[]): LocationDTO {
  if (locations.length === 0) {
    return {
      lng: 137,
      lat: 37,
    };
  }
  const minLng = min(locations.map(it => it.lng));
  const maxLng = max(locations.map(it => it.lng));
  const minLat = min(locations.map(it => it.lat));
  const maxLat = max(locations.map(it => it.lat));
  return {
    lng: (minLng! + maxLng!) / 2,
    lat: (minLat! + maxLat!) / 2,
  };
}

export function determineLevel(locations: LocationDTO[]): number {
  // 척도
  // 3: 50m
  // 4: 100m
  // 5: 250m
  // 6: 500m
  // 7: 1km
  // 8: 2km
  // 9: 4km
  if (locations.length <= 1) {
    return 3;
  }
  const minLng = min(locations.map(it => it.lng))!;
  const maxLng = max(locations.map(it => it.lng))!;
  const minLat = min(locations.map(it => it.lat))!;
  const maxLat = max(locations.map(it => it.lat))!;
  const maxDistance = haversine({ longitude: minLng, latitude: minLat}, { longitude: maxLng, latitude: maxLat }, { unit: 'meter' });
  if (maxDistance < 200) return 3;
  if (maxDistance < 400) return 4;
  if (maxDistance < 1000) return 5;
  if (maxDistance < 2000) return 6;
  if (maxDistance < 4000) return 7;
  if (maxDistance < 8000) return 8;
  if (maxDistance < 16000) return 9;
  return 10;
}
