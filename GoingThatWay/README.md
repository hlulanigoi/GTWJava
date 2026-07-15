# Going That Way — Android App

A crowd-sourced parcel delivery app that connects **senders** with **travelers** going the same direction.  
Built entirely with OpenStreetMap (no Google Maps API key required).

---

## Core Concept

- **Senders** post a parcel with a pickup and destination, then pay via bank transfer. The platform takes 20% and the carrier earns 80%.
- **Travelers** post their trip (origin → destination, departure, transport mode). The app matches parcels whose destinations fall along their route.
- **Tickets** — travelers must buy a carrier ticket (via bank payment) before they can collect parcels.
- **Route matching** — Haversine algorithm checks if a parcel's pickup+destination fall within a 2 km corridor of the trip, with a max 5 km detour.
- **Admin** — in-app admin section (role-based routing) with dashboard, user management, parcel oversight, and payment verification.

---

## Project Structure

```
GoingThatWay/
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/com/goinghatway/app/
│           ├── activities/
│           │   ├── SplashActivity.java        — osmdroid init + role-based routing
│           │   ├── LoginActivity.java         — routes ADMIN → AdminMainActivity
│           │   ├── RegisterActivity.java
│           │   ├── MainActivity.java          — user bottom nav host
│           │   ├── CreateParcelActivity.java  — fee calculator + payment flow
│           │   ├── PostTripActivity.java
│           │   ├── ParcelDetailActivity.java
│           │   ├── TripDetailActivity.java    — shows matches + View Route map
│           │   ├── TripMapActivity.java       — full-screen OSM route map
│           │   ├── BuyTicketActivity.java
│           │   ├── PaymentActivity.java       — bank EFT + reference entry
│           │   └── admin/
│           │       └── AdminMainActivity.java — admin bottom nav host
│           ├── fragments/
│           │   ├── HomeFragment.java          — nearby parcels + embedded OSM map
│           │   ├── ParcelsFragment.java
│           │   ├── TripsFragment.java
│           │   ├── TicketsFragment.java
│           │   ├── ProfileFragment.java
│           │   └── admin/
│           │       ├── AdminDashboardFragment.java  — stats + MPAndroidChart bar chart
│           │       ├── AdminUsersFragment.java      — list + activate/deactivate
│           │       ├── AdminParcelsFragment.java    — list + status filter + cancel
│           │       └── AdminPaymentsFragment.java   — pending payments + verify/reject
│           ├── adapters/
│           │   ├── ParcelAdapter.java
│           │   ├── TripAdapter.java
│           │   ├── TicketAdapter.java
│           │   ├── MatchAdapter.java
│           │   └── admin/
│           │       ├── AdminUserAdapter.java
│           │       ├── AdminParcelAdapter.java
│           │       └── AdminPaymentAdapter.java
│           ├── models/
│           │   ├── User.java        — role field: USER / ADMIN
│           │   ├── Parcel.java
│           │   ├── Trip.java
│           │   ├── Ticket.java
│           │   ├── Match.java
│           │   ├── RoutePoint.java
│           │   └── AdminStats.java
│           ├── viewmodels/
│           │   ├── AuthViewModel.java
│           │   ├── ParcelViewModel.java
│           │   ├── TripViewModel.java
│           │   ├── TicketViewModel.java
│           │   ├── MatchViewModel.java
│           │   └── admin/AdminViewModel.java
│           ├── repositories/
│           │   ├── AuthRepository.java
│           │   ├── ParcelRepository.java
│           │   ├── TripRepository.java
│           │   ├── TicketRepository.java
│           │   ├── MatchRepository.java
│           │   └── admin/AdminRepository.java
│           ├── api/
│           │   ├── ApiService.java
│           │   ├── AdminApiService.java
│           │   ├── ApiClient.java
│           │   └── responses/  ApiResponse, AuthResponse, PaginatedResponse
│           └── utils/
│               ├── Constants.java
│               ├── SessionManager.java      — isAdmin() for role-based routing
│               ├── OsmMapUtils.java         — osmdroid helpers (configure, centerOn, drawRoute, addMarker)
│               └── RouteMatchingUtils.java  — Haversine corridor matching engine
└── res/
    ├── layout/       — all activity + fragment + item XMLs  (green SA eRide theme)
    ├── navigation/   — nav_graph.xml (user), admin_nav_graph.xml (admin)
    ├── menu/         — bottom_nav_menu, admin_bottom_nav_menu, admin_toolbar_menu
    ├── drawable/     — bg_rounded_green, bg_stat_chip, bg_input_field, ic_arrow_back
    └── values/       — colors (#2E7D32 primary), strings, themes, dimens
```

---

## Getting Started

### Requirements

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34 (minSdk 24)

### Setup

1. Open the `GoingThatWay/` folder in Android Studio (**File → Open → select the `GoingThatWay` directory**).
2. Wait for Gradle sync to complete (downloads osmdroid, Retrofit, MPAndroidChart etc.).
3. Point the API base URL to your backend in `app/src/main/java/…/api/ApiClient.java`:
   ```java
   private static final String BASE_URL = "https://api.goinghatway.com/v1/";
   // Local dev:  "http://10.0.2.2:5000/v1/"
   ```
4. Run on a device or emulator (API 24+).

> **No Google Maps API key needed** — the app uses OpenStreetMap via `osmdroid`.

---

## Backend API Contract

| Method | Path | Purpose |
|--------|------|---------|
| POST | `/auth/login` | Login → returns `{ token, user }` |
| POST | `/auth/register` | Register |
| GET | `/auth/me` | Current user |
| GET | `/parcels` | Browse parcels (lat/lng/radius filter) |
| GET | `/parcels/my` | My sent parcels |
| POST | `/parcels` | Create parcel |
| GET | `/trips/my` | My trips |
| POST | `/trips` | Post a trip |
| POST | `/trips/:id/match` | Server-side route match |
| GET | `/matches` | My matches |
| POST | `/matches/:id/accept` | Accept a match |
| POST | `/matches/:id/deliver` | Mark delivered |
| GET | `/tickets/my` | My tickets |
| POST | `/tickets/purchase` | Buy ticket |
| POST | `/payments/verify` | Verify payment reference |
| GET | `/admin/stats` | Admin dashboard stats |
| GET | `/admin/users` | Paginated user list |
| PATCH | `/admin/users/:id/activate` | Activate user |
| PATCH | `/admin/users/:id/deactivate` | Deactivate user |
| GET | `/admin/parcels` | All parcels (filterable) |
| PATCH | `/admin/parcels/:id/status` | Update parcel status |
| GET | `/admin/payments/pending` | Pending bank verifications |
| PATCH | `/admin/payments/:ref/verify` | Verify payment |
| PATCH | `/admin/payments/:ref/reject` | Reject payment |

---

## Business Rules

| Rule | Value | Constant |
|------|-------|----------|
| Platform fee | 20% | `Constants.PLATFORM_FEE_PERCENT` |
| Carrier earning | 80% | `Constants.CARRIER_SHARE_PERCENT` |
| Default ticket price | R 50 | `Constants.DEFAULT_TICKET_PRICE` |
| Route corridor buffer | 2 km | `Constants.ROUTE_BUFFER_KM` |
| Max detour allowed | 5 km | `Constants.MAX_DETOUR_KM` |

---

## Architecture

- **MVVM** — ViewModels + LiveData + Repositories
- **Retrofit 2 + Gson** — REST API
- **Navigation Component** — fragment navigation via bottom nav (user + admin)
- **ViewBinding** — no `findViewById` anywhere
- **osmdroid 6.1.17** — OpenStreetMap (no API key)
- **MPAndroidChart** — admin revenue bar chart
- **Material Components 1.11** — green #2E7D32 brand theme

---

## Admin Access

Log in with an account whose `role` field equals `"ADMIN"` on the server.  
The app automatically routes admins to `AdminMainActivity` on login and splash.

---

## Next Steps

- [ ] Replace placeholder lat/lng (0, 0) in `HomeFragment` with `FusedLocationProviderClient`
- [ ] Add Nominatim / Places geocoding for address → coordinates conversion
- [ ] Add Room entities/DAOs for offline caching
- [ ] Add FCM push notifications for match proposals and delivery updates
- [ ] Add in-app chat between sender and carrier
- [ ] Add post-delivery rating flow
- [ ] Integrate real payment gateway (PayFast / Peach Payments / Paystack) when ready
