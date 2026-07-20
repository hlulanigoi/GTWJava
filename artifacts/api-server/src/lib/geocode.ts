/**
 * Server-side geocoding via Nominatim (OpenStreetMap) — free, no API key.
 * Usage policy requires a descriptive User-Agent and rate limits to 1 req/sec;
 * fine for this app's low-volume parcel/trip creation traffic.
 */
export interface GeocodeResult {
  lat: number;
  lng: number;
}

export async function geocodeAddress(address: string): Promise<GeocodeResult> {
  const url = new URL("https://nominatim.openstreetmap.org/search");
  url.searchParams.set("q", address);
  url.searchParams.set("format", "json");
  url.searchParams.set("limit", "1");

  const res = await fetch(url, {
    headers: {
      "User-Agent": "GoingThatWay/1.0 (taxi-booking-app)",
      Accept: "application/json",
    },
  });

  if (!res.ok) {
    throw new Error(`Geocoding request failed (${res.status})`);
  }

  const results = (await res.json()) as Array<{ lat: string; lon: string }>;
  if (!results.length) {
    throw new Error(`Could not find coordinates for address: "${address}"`);
  }

  return { lat: parseFloat(results[0].lat), lng: parseFloat(results[0].lon) };
}
