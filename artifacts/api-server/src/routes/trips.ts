import { Router, type IRouter } from "express";
import { z } from "zod";
import { and, desc, eq } from "drizzle-orm";
import { db, bookingsTable, ridesTable, tripsTable, usersTable } from "@workspace/db";
import { requireAuth, requireApprovedDriver } from "../middlewares/auth";
import { fail, ok, paginated } from "../lib/response";
import { toBookingJson, toRideJson, toTripJson } from "../lib/serialize";
import { geocodeAddress } from "../lib/geocode";
import { matchRideToRoute } from "../lib/haversine";
import { Constants } from "../lib/constants";

const router: IRouter = Router();

const createTripSchema = z.object({
  origin_address: z.string().min(1),
  destination_address: z.string().min(1),
  departure_time: z.coerce.date(),
  transport_mode: z.enum(["CAR", "BUS", "TRAIN", "WALK", "OTHER"]).default("CAR"),
  seats_available: z.coerce.number().int().positive().default(1),
  notes: z.string().optional(),
});

router.get("/trips", requireAuth, async (req, res) => {
  const page = Math.max(1, Number(req.query.page) || 1);
  const perPage = 20;
  const status = (req.query.status as string) || "ACTIVE";

  const rows = await db.query.tripsTable.findMany({
    where: eq(tripsTable.status, status as any),
    orderBy: desc(tripsTable.createdAt),
  });

  const total = rows.length;
  const pageRows = rows.slice((page - 1) * perPage, page * perPage);
  paginated(res, pageRows.map((t) => toTripJson(t)), total, page, perPage);
});

router.get("/trips/my", requireAuth, async (req, res) => {
  const rows = await db.query.tripsTable.findMany({
    where: eq(tripsTable.driverId, req.user!.userId),
    orderBy: desc(tripsTable.createdAt),
  });
  ok(res, rows.map((t) => toTripJson(t)));
});

router.get("/trips/:id", requireAuth, async (req, res) => {
  const trip = await db.query.tripsTable.findFirst({ where: eq(tripsTable.id, req.params.id as string) });
  if (!trip) return fail(res, "Trip not found", 404);

  const driver = await db.query.usersTable.findFirst({ where: eq(usersTable.id, trip.driverId) });
  const bookings = await db.query.bookingsTable.findMany({ where: eq(bookingsTable.tripId, trip.id) });
  const rides = await Promise.all(
    bookings.map((b) => db.query.ridesTable.findFirst({ where: eq(ridesTable.id, b.rideId) })),
  );

  ok(
    res,
    toTripJson(trip, {
      driver,
      matchedRides: rides.filter(Boolean) as any,
      matchedRideCount: rides.filter(Boolean).length,
    }),
  );
});

router.post("/trips", requireAuth, requireApprovedDriver, async (req, res) => {
  const parsed = createTripSchema.safeParse(req.body);
  if (!parsed.success) return fail(res, parsed.error.issues[0].message, 422);
  const data = parsed.data;

  let origin, destination;
  try {
    [origin, destination] = await Promise.all([
      geocodeAddress(data.origin_address),
      geocodeAddress(data.destination_address),
    ]);
  } catch (err) {
    return fail(res, err instanceof Error ? err.message : "Geocoding failed", 422);
  }

  const [trip] = await db
    .insert(tripsTable)
    .values({
      driverId: req.user!.userId,
      originAddress: data.origin_address,
      originLat: String(origin.lat),
      originLng: String(origin.lng),
      destinationAddress: data.destination_address,
      destinationLat: String(destination.lat),
      destinationLng: String(destination.lng),
      departureTime: data.departure_time,
      transportMode: data.transport_mode,
      seatsAvailable: data.seats_available,
      notes: data.notes,
    })
    .returning();

  ok(res, toTripJson(trip), "Trip created", 201);
});

router.patch("/trips/:id/status", requireAuth, async (req, res) => {
  const schema = z.object({ status: z.enum(["ACTIVE", "COMPLETED", "CANCELLED"]) });
  const parsed = schema.safeParse(req.body);
  if (!parsed.success) return fail(res, parsed.error.issues[0].message, 422);

  const trip = await db.query.tripsTable.findFirst({ where: eq(tripsTable.id, req.params.id as string) });
  if (!trip) return fail(res, "Trip not found", 404);
  if (trip.driverId !== req.user!.userId && req.user!.role !== "ADMIN") {
    return fail(res, "Not authorized to update this trip", 403);
  }

  const [updated] = await db
    .update(tripsTable)
    .set({ status: parsed.data.status })
    .where(eq(tripsTable.id, req.params.id as string))
    .returning();

  ok(res, toTripJson(updated), "Status updated");
});

/** Auto-match this trip's route against all PENDING rides (Haversine corridor check). */
router.post("/trips/:id/match", requireAuth, requireApprovedDriver, async (req, res) => {
  const trip = await db.query.tripsTable.findFirst({ where: eq(tripsTable.id, req.params.id as string) });
  if (!trip) return fail(res, "Trip not found", 404);
  if (trip.driverId !== req.user!.userId) {
    return fail(res, "Not authorized to match this trip", 403);
  }

  const pendingRides = await db.query.ridesTable.findMany({
    where: eq(ridesTable.status, "PENDING"),
  });

  const origin = { lat: Number(trip.originLat), lng: Number(trip.originLng) };
  const destination = { lat: Number(trip.destinationLat), lng: Number(trip.destinationLng) };

  const createdBookings = [];
  for (const ride of pendingRides) {
    const pickup = { lat: Number(ride.pickupLat), lng: Number(ride.pickupLng) };
    const dropoff = { lat: Number(ride.destinationLat), lng: Number(ride.destinationLng) };

    const result = matchRideToRoute(
      origin,
      destination,
      pickup,
      dropoff,
      Constants.ROUTE_BUFFER_KM,
      Constants.MAX_DETOUR_KM,
    );
    if (!result) continue;

    const existing = await db.query.bookingsTable.findFirst({
      where: and(eq(bookingsTable.tripId, trip.id), eq(bookingsTable.rideId, ride.id)),
    });
    if (existing) continue;

    const [booking] = await db
      .insert(bookingsTable)
      .values({
        tripId: trip.id,
        rideId: ride.id,
        driverId: trip.driverId,
        riderId: ride.riderId,
        score: String(Math.round(result.score * 100) / 100),
        alongRoute: result.isAlongRoute,
        detourKm: String(Math.round(result.detourKm * 100) / 100),
        driverEarning: ride.driverEarning,
      })
      .returning();

    createdBookings.push(toBookingJson(booking, { ride, trip }));
  }

  ok(res, createdBookings, `${createdBookings.length} booking(s) found`);
});

export default router;
