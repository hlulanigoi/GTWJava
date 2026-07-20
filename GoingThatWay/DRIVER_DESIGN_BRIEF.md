# Going That Way — Driver App Design Brief

> Living document. Update this when major design decisions are made.

---

## Visual Theme: Highveld Night

| Token              | Value       | Rationale                                     |
|--------------------|-------------|-----------------------------------------------|
| Background         | `#121212`   | True dark — reduces eye strain on night runs  |
| Surface            | `#1E1E1E`   | Card/panel separation from background         |
| Surface elevated   | `#2A2A2A`   | Chips, inner panels                           |
| Primary (CTA)      | `#00C853`   | Electric green — action, trust, go            |
| Accent (earnings)  | `#FFB300`   | Amber — money, urgency, countdown             |
| Text primary       | `#F5F5F5`   | Near-white, comfortable at any brightness     |
| Text secondary     | `#9E9E9E`   | Labels, hints, helper text                    |
| Divider            | `#2E2E2E`   | Subtle separation                             |

Applied via driver module resource overrides (`driver/res/values/colors.xml`, `driver/res/values/themes.xml`).  
Does **not** touch the shared or requester module — they keep the light green palette.

---

## Screen Inventory

| Screen                    | File                                    | Status     |
|---------------------------|-----------------------------------------|------------|
| Driver dashboard          | `fragment_driver_dashboard.xml`         | ✅ Rebuilt  |
| Trip request offer card   | `activity_trip_requests.xml`            | ✅ Rebuilt  |
| Trip posting form         | `activity_post_trip.xml` (shared)       | ✅ Rebuilt  |
| Earnings                  | `activity_earnings.xml`                 | ✅ Rebuilt  |
| Trip list item            | `item_trip.xml`                         | ✅ Rebuilt  |
| Main navigation           | `activity_main_driver.xml`              | ✅ Rebuilt  |
| Trip list host            | `activity_trip_list.xml`               | ✅ Rebuilt  |

---

## Competitive Research

### Uber Driver (Destination Mode)
- Driver sets their **own** travel destination; the app matches only riders/senders going the same way.
- May 2026 update: improved corridor matching to ensure each trip moves the driver *closer* to their destination.
- Key UX insight: **the driver's route is the product** — the form is a route declaration, not a booking request.
- Takeaway for GTW: the trip posting form should feel like declaring a journey, not filling out a shipping form.

### Bolt Driver Destinations (SA launch, March 2024)
- Same concept: driver sets destination, only gets ride requests along their corridor.
- **Radius filter** (0.6–5.3 miles off-route) — driver controls how far they'll deviate.
- Google Maps renders the route with the corridor visually.
- Takeaway for GTW: expose `corridor_km` as a **slider** so drivers know exactly what they're committing to.

### inDrive (formerly inDriver)
- Passengers propose a fare; drivers can **counter-offer** a different price; negotiation happens in-app.
- Recommended minimum bidding fare varies by location and route.
- Drivers see the offered fare upfront — no black-box algorithm.
- Takeaway for GTW: add **minimum payout preference** to the trip form (filter below a threshold), and add a **counter-offer** button on the request card (not just binary accept/decline).

---

## Offer Card Design (20-second rule)

Following VP0's research principle: **the offer card must be readable at windshield distance in under 2 seconds**.

Rules applied:
- Max 4 data points: payout (amber, display-size), route, time, distance
- `CircularProgressIndicator` countdown ring, 20 s, 100 ms interval — auto-declines on expiry
- `ACCEPT` dominant (full-width, electric green, 56 dp tall)
- Counter-offer reveals a panel (inDrive-inspired) — driver types an alternative amount, sends it
- Decline is text-button weight — present but not primary

---

## Trip Posting Form Improvements

### What was replaced

| Old                          | New                                  | Why                            |
|------------------------------|--------------------------------------|--------------------------------|
| Plain `EditText` origin/dest | `AutoCompleteTextView` + Nominatim   | Real SA address search, no key |
| Free-text departure time     | `MaterialDatePicker` + `TimePicker`  | Prevents format errors         |
| Android `Spinner` for mode   | `ChipGroup` (Car / Bus / Train / …)  | Scannable, Material, glanceable|
| No corridor preference       | Slider 1–20 km (Bolt-inspired)       | Driver owns their route buffer |
| No fare floor                | "Min payout" field (inDrive-inspired)| Filters requests below threshold|

### New Java classes (shared module)

| Class                          | Purpose                                                  |
|--------------------------------|----------------------------------------------------------|
| `NominatimPlace`               | POJO for one OSM geocoding result                        |
| `NominatimService`             | Retrofit interface → `nominatim.openstreetmap.org/search`|
| `NominatimAutocompleteHelper`  | Debounced (450 ms) autocomplete manager, SA-biased       |

### API changes
`TripRepository.createTrip()` now sends two extra fields:

```json
{
  "corridor_km": 5,
  "min_fare": 80.0
}
```

The backend should use `corridor_km` instead of the global `ROUTE_BUFFER_KM` constant when it is present, and skip route matches with a payout below `min_fare`.

---

## Counter-Offer Flow (inDrive-inspired)

```
Driver sees request card
  → ACCEPT  → accepted, navigate to DriverTripLiveActivity
  → Make Counter-Offer  → reveals inline panel
      Driver types amount → Send → card collapses, status = "Waiting for sender…"
      (TODO: POST /api/bookings/{id}/counter { amount })
  → Decline → card collapses, status = "Declined"
  → [20 s expire] → auto-decline, toast "Request expired"
```

---

## Nominatim Usage Policy Compliance

- User-Agent header: `GoingThatWay Android App contact@goingthatway.app`
- Debounce: 450 ms — well within 1-req/s limit
- `countrycodes=za` — biases results to South Africa, reduces irrelevant results
- Max 6 results per call — limits payload and render cost

---

## Outstanding TODOs

- [ ] Wire `POST /api/bookings/{id}/counter` API endpoint (backend + Android)
- [ ] Backend: honour `corridor_km` from trip body (currently uses global constant)
- [ ] Backend: honour `min_fare` filter in route matching
- [ ] Map preview in trip form — small osmdroid MapView showing A→B corridor (infrastructure is in `OsmMapUtils.java`)
- [ ] `TripFormActivity` (driver module placeholder) — redirect to `PostTripActivity` or remove duplicate
- [ ] Live Activity / lock screen trip state (Android equivalent: foreground service notification)
