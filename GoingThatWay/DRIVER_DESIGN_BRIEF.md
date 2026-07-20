# Going That Way — Driver App Design Brief

## Research Summary

### What competitors do well

| App | Key UX win |
|-----|-----------|
| **Uber Driver** | Map-dominant dashboard; online/offline toggle fills the hero; earnings shown prominently at the top |
| **Lalamove Driver** | Bold orange request cards; fare is the largest number on screen; countdown ring on every request |
| **PickMe Driver** | Redesigned for one-handed use in direct sunlight — 15 % accidental-rejection rate → near zero after redesign |
| **DoorDash** | "Dash Now" CTA is enormous; status chip changes colour with every state transition |

### The universal rules for production driver apps

1. **Glance-safety first.** Drivers read the screen for 1–2 seconds while parked in traffic. Every critical number (fare, distance, payout) must be readable at windshield distance — think 36 sp+ for key figures.
2. **One decision per screen.** The trip-request card must have exactly two actions: Accept (dominant) and Decline (present but secondary). No other tappable elements during the countdown.
3. **Online/offline is a right, not a UX afterthought.** The toggle must be impossible to miss and must not use guilt-tripping copy.
4. **Earnings transparency builds trust.** Show the per-trip breakdown; drivers who see the math stay engaged longer.
5. **State machine clarity.** Every trip state (MATCHED → COLLECTED → DELIVERED) needs a distinct colour, icon, and one obvious next action.

---

## Proposed Theme: "Highveld Night"

A dark-surface, high-contrast theme built for South African conditions — harsh sunlight, one-handed use, township connectivity.

### Colour Palette

```
Background (surface)   #121212   — near-black, easy on OLED
Surface elevated       #1E1E1E   — card backgrounds
Surface overlay        #2A2A2A   — input fields, chips

Brand green (primary)  #00C853   — electric lime-green, readable on dark
Brand green pressed    #00A846   — pressed state
Amber (earnings/CTA)   #FFB300   — kept from current accent, now glows on dark

Status — Pending       #FF6D00   — deep orange
Status — Matched       #2979FF   — electric blue  
Status — Collected     #D500F9   — vivid purple
Status — Delivered     #00C853   — green (same as primary)
Status — Cancelled     #F44336   — material red

Text primary           #FFFFFF   — body on dark
Text secondary         #9E9E9E   — labels, captions
Divider                #333333   — subtle separators
```

### Typography

- **Google font: Space Grotesk** — modern, slightly technical, great legibility; replaces default sans-serif
- Fare / payout figures: **48 sp Bold** (read at arm's length)
- Section headers: **20 sp SemiBold**
- Body: **15 sp Regular**
- Labels / status chips: **12 sp Medium**, ALL CAPS

### Component redesign targets

#### 1. Driver Dashboard (`fragment_driver_dashboard.xml`)
**Current:** plain LinearLayout, one button, static text  
**Proposed:**
- Full-screen dark surface
- Top strip: driver avatar + name + online status pill (green pulse animation when online)
- **Hero card**: today's earnings figure `R 0.00` in 48 sp amber, "trips today" counter below
- Bottom half: large online/offline FAB (green glow when online, grey when offline)
- Stats row: trips · distance · rating in three equal chips

#### 2. Trip Request Card (`activity_trip_requests.xml` + `item_trip.xml`)
**Current:** two plain TextViews  
**Proposed (the "offer card" — most critical screen):**
- Full-screen modal / bottom sheet with dark overlay
- **Fare** — 48 sp, amber, top-left: `R 45.00`
- Parcel route: origin → destination with distance tag
- Pickup detour: `+2.1 km off your route`
- Parcel info chip: weight, size category
- **Countdown ring** (circular progress, 20 s) — server-anchored
- `ACCEPT` — full-width green button, 56 dp tall
- `DECLINE` — text button, no guilt copy, always visible

#### 3. Earnings (`activity_earnings.xml`)
**Current:** three TextViews  
**Proposed:**
- Period toggle: Today / Week / Month (segmented control, green underline)
- Hero: total payout in 48 sp amber
- Stat row: Trips · Avg per trip · Distance — matching stat chips
- Bar chart: last 7 days' earnings (simple custom view or MPAndroidChart)
- Per-trip breakdown list below

#### 4. Trip List (`activity_trip_list.xml` + `item_trip.xml`)
**Current:** single TextView in a grey box  
**Proposed card:**
- Dark card (`#1E1E1E`) with 12 dp corners
- Route: origin city → destination city (bold, one line each)
- Date + time badge (top-right)
- Status chip (colour-coded, per palette above)
- Parcel count badge + fare on the bottom row

#### 5. Bottom navigation
**Current:** stock MaterialComponents BottomNav  
**Proposed:**
- Dark background (`#1E1E1E`), no elevation shadow
- Active icon tint: `#00C853` (brand green)
- Inactive tint: `#616161`
- Selected item has a small green pill indicator, not underline
- Icons: Dashboard · My Trips · Earnings · Profile

---

## Files to change (implementation scope)

| File | Change |
|------|--------|
| `shared/.../values/colors.xml` | Full palette swap to Highveld Night |
| `shared/.../values/themes.xml` | Parent → `Theme.MaterialComponents.DayNight.NoActionBar`; add new component styles |
| `shared/.../values/dimens.xml` | Add `text_hero`, `text_display` sizes |
| `driver/.../fragment_driver_dashboard.xml` | Full rebuild — dark hero + stats + FAB |
| `driver/.../activity_trip_requests.xml` | Offer card bottom sheet |
| `driver/.../item_trip.xml` | Dark card layout |
| `driver/.../activity_earnings.xml` | Earnings hero + stats + list |
| `driver/.../activity_main_driver.xml` | Dark nav bar |
| `shared/.../res/drawable/` | New drawables: online pill, countdown ring, stat chips |

---

## What is NOT changing in this proposal

- Package names, module structure, Java source logic
- API contracts / backend
- Android manifest
- Requester or Admin UI (separate task if needed)
