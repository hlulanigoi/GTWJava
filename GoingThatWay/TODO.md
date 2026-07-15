# Going That Way — TODO Before Deployment

---

## 🔴 CRITICAL — App won't work without these

- [ ] **Build backend API server** — implement all REST endpoints (see list below)
  - [ ] `POST /auth/login`
  - [ ] `POST /auth/register`
  - [ ] `GET  /auth/me`
  - [ ] `GET  /parcels` (with lat/lng/radius filter)
  - [ ] `GET  /parcels/my`
  - [ ] `POST /parcels`
  - [ ] `GET  /parcels/:id`
  - [ ] `GET  /trips/my`
  - [ ] `POST /trips`
  - [ ] `GET  /trips/:id`
  - [ ] `POST /trips/:id/match` (Haversine route-matching logic)
  - [ ] `GET  /matches`
  - [ ] `POST /matches/:id/accept`
  - [ ] `POST /matches/:id/deliver`
  - [ ] `GET  /tickets/my`
  - [ ] `POST /tickets/purchase`
  - [ ] `POST /payments/verify`
  - [ ] `GET  /admin/stats`
  - [ ] `GET  /admin/users`
  - [ ] `PATCH /admin/users/:id/activate`
  - [ ] `PATCH /admin/users/:id/deactivate`
  - [ ] `GET  /admin/parcels`
  - [ ] `PATCH /admin/parcels/:id/status`
  - [ ] `GET  /admin/payments/pending`
  - [ ] `PATCH /admin/payments/:ref/verify`
  - [ ] `PATCH /admin/payments/:ref/reject`

- [ ] **Build database schema** — define all Drizzle ORM tables and run migrations
  - [ ] `users` table (id, full_name, email, phone, password_hash, role, rating, active)
  - [ ] `parcels` table (id, sender_id, description, weight, size, pickup_*, dest_*, status, fee, payment_ref)
  - [ ] `trips` table (id, carrier_id, origin_*, dest_*, departure, arrival, transport_mode, capacity, status)
  - [ ] `tickets` table (id, user_id, code, price_paid, payment_ref, status, expires_at)
  - [ ] `matches` table (id, trip_id, parcel_id, score, status, along_route)
  - [ ] `payments` table (id, user_id, reference, amount, type, status, verified_by)

- [ ] **Address geocoding** — convert entered text addresses to lat/lng before saving
  - [ ] All pickup/destination lat/lng currently default to `0.0` — Haversine matching is broken
  - [ ] Use Nominatim (free, OSM-based) on the server side, or add geocoding call in `CreateParcelActivity` / `PostTripActivity`

- [ ] **Runtime location permission request**
  - [ ] `HomeFragment` and `TripMapActivity` call `addMyLocation()` but never request `ACCESS_FINE_LOCATION` at runtime
  - [ ] Add permission check/request in both places (Android 6+ requirement)

---

## 🟡 HIGH — Major features broken

- [ ] **Audit all user-facing adapters for view-ID mismatches**
  - [ ] `ParcelAdapter.java` — verify IDs match `item_parcel.xml` (tv_description, tv_pickup, tv_destination, tv_weight, tv_earnings, tv_status)
  - [ ] `TripAdapter.java` — verify IDs match `item_trip.xml`
  - [ ] `TicketAdapter.java` — verify IDs match `item_ticket.xml` (tv_ticket_code, tv_purchased_at, tv_expires_at, tv_price_paid, tv_status)
  - [ ] `MatchAdapter.java` — verify IDs match `item_match.xml`

- [ ] **Fix `PaginatedResponse<T>` field naming inconsistency**
  - [ ] `HomeFragment` calls `response.getData().getData()`
  - [ ] `AdminRepository` calls `response.getData().getItems()`
  - [ ] One is wrong — standardise the field name across both

- [ ] **Verify `BuyTicketActivity` binding after layout redesign**
  - [ ] Layout was redesigned; activity binding references may be stale
  - [ ] Check `btn_buy_ticket`, `tv_ticket_price`, `progress_bar`

- [ ] **Server-generated payment reference**
  - [ ] `PaymentActivity` currently generates `"GTW-" + System.currentTimeMillis()` client-side
  - [ ] Should be generated server-side (UUID), returned to the app, displayed for the user to use in their bank transfer

---

## 🟢 IMPORTANT — Not blocking local testing but required before release

- [ ] **Verify JWT auth interceptor in `ApiClient.java`**
  - [ ] Confirm `OkHttp` interceptor reads token from `SessionManager` and attaches `Authorization: Bearer <token>` on every request

- [ ] **Handle token expiry / 401 responses**
  - [ ] If server returns 401, clear session and redirect to `LoginActivity` instead of showing a network error

- [ ] **Replace `0, 0` GPS placeholder with real device location**
  - [ ] `HomeFragment.loadParcels()` passes `0, 0` as lat/lng — nearby parcel list is meaningless
  - [ ] Integrate `FusedLocationProviderClient` to get last-known location first

- [ ] **Haversine server implementation**
  - [ ] Port `RouteMatchingUtils.java` logic to the backend
  - [ ] `POST /trips/:id/match` should compare the trip's route against all PENDING parcels and return scored matches

- [ ] **Admin revenue chart data**
  - [ ] `AdminStats.getRevenueThisWeek()` and `getWeekLabels()` must be returned by the server
  - [ ] Server needs to aggregate daily revenue for the past 7 days

- [ ] **Parcel photo upload (optional feature)**
  - [ ] `item_parcel.xml` and `activity_parcel_detail.xml` have an `iv_parcel_image` placeholder
  - [ ] Add camera/gallery picker + multipart upload endpoint if photos are part of the product

- [ ] **FCM push notifications**
  - [ ] Carrier notified when a parcel matches their trip
  - [ ] Sender notified when parcel is collected / delivered
  - [ ] Requires Firebase project setup + `google-services.json` + token registration endpoint

---

## 🔵 PRE-LAUNCH POLISH

- [ ] **Offline fallback** — add Room caching so the app isn't blank with no signal
- [ ] **Global error handling** — network error snackbar with retry button across all screens
- [ ] **Post-delivery rating flow** — sender rates carrier (and vice versa) after delivery confirmed
- [ ] **Carrier ↔ sender contact** — in-app chat or WhatsApp deeplink once a match is accepted
- [ ] **Deep-link parcel tracking** — shareable link so sender can track parcel status
- [ ] **ProGuard rules** — add rules for Retrofit, Gson, osmdroid in `proguard-rules.pro`
- [ ] **Release signing** — create `keystore.jks`, add `signingConfigs` block to `app/build.gradle`
- [ ] **Play Store assets** — full-res app icon, feature graphic, screenshots, privacy policy URL, short/long descriptions
- [ ] **Privacy policy page** — required by Google Play for apps requesting location permission

---

## Suggested Build Order

```
Week 1  → DB schema + auth endpoints + JWT interceptor check
Week 2  → Parcel + Trip + Ticket + Match endpoints + Haversine server logic
Week 3  → Admin endpoints + geocoding + runtime permissions
Week 4  → Adapter ID audit + PaginatedResponse fix + BuyTicket fix + server-side payment ref
Week 5  → FCM notifications + 401 handling + FusedLocation GPS
Week 6  → Room caching + ProGuard + signing + Play Store assets + submission
```
