import { Router, type IRouter } from "express";
import { z } from "zod";
import { and, desc, eq } from "drizzle-orm";
import { db, ridesTable, bookingsTable, tripsTable, usersTable } from "@workspace/db";
import { requireAuth, requireApprovedDriver } from "../middlewares/auth";
import { fail, ok, paginated } from "../lib/response";
import { toRideJson, toBookingJson } from "../lib/serialize";
import { geocodeAddress } from "../lib/geocode";
import { haversineKm, matchRideToRoute } from "../lib/haversine";
import { Constants } from "../lib/constants";

const router: IRouter = Router();

const createRideSchema = z.object({
  ride_type: z.enum(["SCHEDULED", "ON_DEMAND"]).default("ON_DEMAND"),
  pickup_address: z.string().min(1),
  destination_address: z.string().min(1),
  passenger_count: z.coerce.number().int().positive().default(1),
  luggage_size: z.enum(["NONE", "SMALL", "MEDIUM", "LARGE"]).default("NONE"),
  notes: z.string().optional(),
  payment_reference: z.string().min(1),
});

const onDemandRideSchema = z.object({
  pickup_lat: z.coerce.number(),
  pickup_lng: z.coerce.number(),
  destination_address: z.string().min(1),
  passenger_count: z.coerce.number().int().positive().default(1),
  luggage_size: z.enum(["NONE", "SMALL", "MEDIUM", "LARGE"]).default("NONE"),
  notes: z.string().optional(),
  payment_reference: z.string().min(1),
});

function calculateFare(passengerCount: number) {
  const fare = Math.round((50 + passengerCount * 20) * 100) / 100;
  const driverEarning = Math.round(fare * Constants.DRIVER_SHARE_PERCENT * 100) / 100;
  const platformFee = Math.round((fare - driverEarning) * 100) / 100;
  return { fare, driverEarning, platformFee };
}

router.get("/rides", requireAuth, async (req, res) => {
  const page = Math.max(1, Number(req.query.page) || 1);
  const perPage = 20;
  const status = (req.query.status as string) || "PENDING";
  const lat = req.query.lat !== undefined ? Number(req.query.lat) : undefined;
  const lng = req.query.lng !== undefined ? Number(req.query.lng) : undefined;
  const radiusKm = req.query.radius_km !== undefined ? Number(req.query.radius_km) : undefined;

  const rows = await db.query.ridesTable.findMany({
    where: eq(ridesTable.status, status as any),
    orderBy: desc(ridesTable.createdAt),
  });

  const filtered =
    lat !== undefined && lng !== undefined && radiusKm !== undefined
      ? rows.filter(
          (r) =>
            haversineKm({ lat, lng }, { lat: Number(r.pickupLat), lng: Number(r.pickupLng) }) <=
            radiusKm,
        )
      : rows;

  const total = filtered.length;
  const pageRows = filtered.slice((page - 1) * perPage, page * perPage);
  paginated(res, pageRows.map((r) => toRideJson(r)), total, page, perPage);
});

router.get("/rides/my", requireAuth, async (req, res) => {
  const rows = await db.query.ridesTable.findMany({
    where: eq(ridesTable.riderId, req.user!.userId),
    orderBy: desc(ridesTable.createdAt),
  });
  ok(res, rows.map((r) => toRideJson(r)));
});

router.post("/rides/on-demand", requireAuth, async (req, res) => {
  const parsed = onDemandRideSchema.safeParse(req.body);
  if (!parsed.success) return fail(res, parsed.error.issues[0].message, 422);
  const data = parsed.data;

  let destination;
  try {
    destination = await geocodeAddress(data.destination_address);
  } catch (err) {
    return fail(res, err instanceof Error ? err.message : "Geocoding failed", 422);
  }

  const { fare, driverEarning, platformFee } = calculateFare(data.passenger_count);

  const [ride] = await db
    .insert(ridesTable)
    .values({
      riderId: req.user!.userId,
      rideType: "ON_DEMAND",
      notes: data.notes,
      passengerCount: data.passenger_count,
      luggageSize: data.luggage_size,
      pickupAddress: `${data.pickup_lat},${data.pickup_lng}`,
      pickupLat: String(data.pickup_lat),
      pickupLng: String(data.pickup_lng),
      destinationAddress: data.destination_address,
      destinationLat: String(destination.lat),
      destinationLng: String(destination.lng),
      fare: String(fare),
      driverEarning: String(driverEarning),
      platformFee: String(platformFee),
      paymentReference: data.payment_reference,
    })
    .returning();

  // Try to match against active trips immediately
  const activeTrips = await db.query.tripsTable.findMany({
    where: eq(tripsTable.status, "ACTIVE"),
  });

  const ridePickup = { lat: Number(ride.pickupLat), lng: Number(ride.pickupLng) };
  const rideDest = { lat: Number(ride.destinationLat), lng: Number(ride.destinationLng) };

  let immediateBooking = null;
  for (const trip of activeTrips) {
    const origin = { lat: Number(trip.originLat), lng: Number(trip.originLng) };
    const dest = { lat: Number(trip.destinationLat), lng: Number(trip.destinationLng) };

    const result = matchRideToRoute(origin, dest, ridePickup, rideDest, Constants.ROUTE_BUFFER_KM, Constants.MAX_DETOUR_KM);
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

    immediateBooking = toBookingJson(booking, { ride, trip });
    break; // take first match
  }

  ok(
    res,
    { ride: toRideJson(ride), booking: immediateBooking },
    immediateBooking ? "Ride created and matched" : "Ride created, awaiting driver",
    201,
  );
});

router.get("/rides/:id", requireAuth, async (req, res) => {
  const ride = await db.query.ridesTable.findFirst({
    where: eq(ridesTable.id, req.params.id as string),
  });
  if (!ride) return fail(res, "Ride not found", 404);

  const [rider, driver] = await Promise.all([
    db.query.usersTable.findFirst({ where: eq(usersTable.id, ride.riderId) }),
    ride.driverId
      ? db.query.usersTable.findFirst({ where: eq(usersTable.id, ride.driverId) })
      : Promise.resolve(null),
  ]);

  ok(res, toRideJson(ride, { rider, driver }));
});

router.post("/rides", requireAuth, async (req, res) => {
  const parsed = createRideSchema.safeParse(req.body);
  if (!parsed.success) return fail(res, parsed.error.issues[0].message, 422);
  const data = parsed.data;

  let pickup, destination;
  try {
    [pickup, destination] = await Promise.all([
      geocodeAddress(data.pickup_address),
      geocodeAddress(data.destination_address),
    ]);
  } catch (err) {
    return fail(res, err instanceof Error ? err.message : "Geocoding failed", 422);
  }

  const { fare, driverEarning, platformFee } = calculateFare(data.passenger_count);

  const [ride] = await db
    .insert(ridesTable)
    .values({
      riderId: req.user!.userId,
      rideType: data.ride_type,
      notes: data.notes,
      passengerCount: data.passenger_count,
      luggageSize: data.luggage_size,
      pickupAddress: data.pickup_address,
      pickupLat: String(pickup.lat),
      pickupLng: String(pickup.lng),
      destinationAddress: data.destination_address,
      destinationLat: String(destination.lat),
      destinationLng: String(destination.lng),
      fare: String(fare),
      driverEarning: String(driverEarning),
      platformFee: String(platformFee),
      paymentReference: data.payment_reference,
    })
    .returning();

  ok(res, toRideJson(ride), "Ride created", 201);
});

router.patch("/rides/:id/status", requireAuth, async (req, res) => {
  const schema = z.object({
    status: z.enum(["PENDING", "MATCHED", "EN_ROUTE", "COMPLETED", "CANCELLED"]),
  });
  const parsed = schema.safeParse(req.body);
  if (!parsed.success) return fail(res, parsed.error.issues[0].message, 422);

  const ride = await db.query.ridesTable.findFirst({
    where: eq(ridesTable.id, req.params.id as string),
  });
  if (!ride) return fail(res, "Ride not found", 404);
  if (ride.riderId !== req.user!.userId && ride.driverId !== req.user!.userId && req.user!.role !== "ADMIN") {
    return fail(res, "Not authorized to update this ride", 403);
  }

  const [updated] = await db
    .update(ridesTable)
    .set({ status: parsed.data.status })
    .where(eq(ridesTable.id, req.params.id as string))
    .returning();

  ok(res, toRideJson(updated), "Status updated");
});

router.delete("/rides/:id", requireAuth, async (req, res) => {
  const ride = await db.query.ridesTable.findFirst({
    where: eq(ridesTable.id, req.params.id as string),
  });
  if (!ride) return fail(res, "Ride not found", 404);
  if (ride.riderId !== req.user!.userId && req.user!.role !== "ADMIN") {
    return fail(res, "Not authorized to delete this ride", 403);
  }
  if (ride.status !== "PENDING") {
    return fail(res, "Only pending rides can be deleted", 409);
  }

  await db.delete(ridesTable).where(eq(ridesTable.id, req.params.id as string));
  ok(res, null, "Ride deleted");
});

export default router;
