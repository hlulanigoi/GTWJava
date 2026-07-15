/**
 * Haversine distance + route-corridor matching.
 * Mirrors the client-side RouteMatchingUtils.java logic so a trip's route can be
 * matched against pending parcels server-side (POST /trips/:id/match).
 */
const EARTH_RADIUS_KM = 6371;

export interface LatLng {
  lat: number;
  lng: number;
}

export function haversineKm(a: LatLng, b: LatLng): number {
  const dLat = toRad(b.lat - a.lat);
  const dLng = toRad(b.lng - a.lng);
  const lat1 = toRad(a.lat);
  const lat2 = toRad(b.lat);

  const h =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLng / 2) ** 2;

  return 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(h));
}

function toRad(deg: number): number {
  return (deg * Math.PI) / 180;
}

/** Project point p onto segment a→b and return the distance from p to that projection, in km. */
export function distanceToSegmentKm(p: LatLng, a: LatLng, b: LatLng): number {
  // Work in a local equirectangular projection (fine for short trip-scale distances).
  const toXY = (pt: LatLng) => ({
    x: pt.lng * Math.cos(toRad(a.lat)),
    y: pt.lat,
  });

  const A = toXY(a);
  const B = toXY(b);
  const P = toXY(p);

  const abx = B.x - A.x;
  const aby = B.y - A.y;
  const apx = P.x - A.x;
  const apy = P.y - A.y;

  const lenSq = abx * abx + aby * aby;
  let t = lenSq === 0 ? 0 : (apx * abx + apy * aby) / lenSq;
  t = Math.max(0, Math.min(1, t));

  const projLat = A.y + t * aby;
  const projLngXY = A.x + t * abx;
  const projLng = projLngXY / Math.cos(toRad(a.lat));

  return haversineKm(p, { lat: projLat, lng: projLng });
}

export interface MatchResult {
  isAlongRoute: boolean;
  detourKm: number;
  score: number;
}

/**
 * Score how well a parcel (pickup -> destination) fits along a trip's route (origin -> destination).
 * `bufferKm` is the corridor half-width considered "along route" (no real detour).
 * `maxDetourKm` is the max extra distance still considered a viable match.
 */
export function matchParcelToRoute(
  origin: LatLng,
  destination: LatLng,
  pickup: LatLng,
  dropoff: LatLng,
  bufferKm = 2,
  maxDetourKm = 5,
): MatchResult | null {
  const pickupDetour = distanceToSegmentKm(pickup, origin, destination);
  const dropoffDetour = distanceToSegmentKm(dropoff, origin, destination);
  const detourKm = Math.max(pickupDetour, dropoffDetour);

  if (detourKm > maxDetourKm) return null;

  const isAlongRoute = detourKm <= bufferKm;
  const score = Math.max(0, 1 - detourKm / maxDetourKm);

  return { isAlongRoute, detourKm, score };
}
