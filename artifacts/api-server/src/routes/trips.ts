import { Router, type IRouter } from "express";
import { z } from "zod";
import { and, desc, eq } from "drizzle-orm";
import { db, matchesTable, parcelsTable, tripsTable, usersTable } from "@workspace/db";
import { requireAuth } from "../middlewares/auth";
import { fail, ok, paginated } from "../lib/response";
import { toMatchJson, toParcelJson, toTripJson } from "../lib/serialize";
import { geocodeAddress } from "../lib/geocode";
import { matchParcelToRoute } from "../lib/haversine";
import { Constants } from "../lib/constants";

const router: IRouter = Router();

const createTripSchema = z.object({
  origin_address: z.string().min(1),
  destination_address: z.string().min(1),
  departure_time: z.coerce.date(),
  transport_mode: z.enum(["CAR", "BUS", "TRAIN", "WALK", "OTHER"]).default("CAR"),
  available_capacity_kg: z.coerce.number().positive(),
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
    where: eq(tripsTable.carrierId, req.user!.userId),
    orderBy: desc(tripsTable.createdAt),
  });
  ok(res, rows.map((t) => toTripJson(t)));
});

router.get("/trips/:id", requireAuth, async (req, res) => {
  const trip = await db.query.tripsTable.findFirst({ where: eq(tripsTable.id, (req.params.id as string)) });
  if (!trip) return fail(res, "Trip not found", 404);

  const traveler = await db.query.usersTable.findFirst({ where: eq(usersTable.id, trip.carrierId) });
  const matches = await db.query.matchesTable.findMany({ where: eq(matchesTable.tripId, trip.id) });
  const parcels = await Promise.all(
    matches.map((m) => db.query.parcelsTable.findFirst({ where: eq(parcelsTable.id, m.parcelId) })),
  );

  ok(
    res,
    toTripJson(trip, {
      traveler,
      matchedParcels: parcels.filter(Boolean) as any,
      matchedParcelCount: parcels.filter(Boolean).length,
    }),
  );
});

router.post("/trips", requireAuth, async (req, res) => {
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
      carrierId: req.user!.userId,
      originAddress: data.origin_address,
      originLat: String(origin.lat),
      originLng: String(origin.lng),
      destinationAddress: data.destination_address,
      destinationLat: String(destination.lat),
      destinationLng: String(destination.lng),
      departureTime: data.departure_time,
      transportMode: data.transport_mode,
      capacityKg: String(data.available_capacity_kg),
      notes: data.notes,
    })
    .returning();

  ok(res, toTripJson(trip), "Trip created", 201);
});

router.patch("/trips/:id/status", requireAuth, async (req, res) => {
  const schema = z.object({ status: z.enum(["ACTIVE", "COMPLETED", "CANCELLED"]) });
  const parsed = schema.safeParse(req.body);
  if (!parsed.success) return fail(res, parsed.error.issues[0].message, 422);

  const trip = await db.query.tripsTable.findFirst({ where: eq(tripsTable.id, (req.params.id as string)) });
  if (!trip) return fail(res, "Trip not found", 404);
  if (trip.carrierId !== req.user!.userId && req.user!.role !== "ADMIN") {
    return fail(res, "Not authorized to update this trip", 403);
  }

  const [updated] = await db
    .update(tripsTable)
    .set({ status: parsed.data.status })
    .where(eq(tripsTable.id, (req.params.id as string)))
    .returning();

  ok(res, toTripJson(updated), "Status updated");
});

/** Auto-match this trip's route against all PENDING parcels (Haversine corridor check). */
router.post("/trips/:id/match", requireAuth, async (req, res) => {
  const trip = await db.query.tripsTable.findFirst({ where: eq(tripsTable.id, (req.params.id as string)) });
  if (!trip) return fail(res, "Trip not found", 404);
  if (trip.carrierId !== req.user!.userId) {
    return fail(res, "Not authorized to match this trip", 403);
  }

  const pendingParcels = await db.query.parcelsTable.findMany({
    where: eq(parcelsTable.status, "PENDING"),
  });

  const origin = { lat: Number(trip.originLat), lng: Number(trip.originLng) };
  const destination = { lat: Number(trip.destinationLat), lng: Number(trip.destinationLng) };

  const createdMatches = [];
  for (const parcel of pendingParcels) {
    const pickup = { lat: Number(parcel.pickupLat), lng: Number(parcel.pickupLng) };
    const dropoff = { lat: Number(parcel.destinationLat), lng: Number(parcel.destinationLng) };

    const result = matchParcelToRoute(
      origin,
      destination,
      pickup,
      dropoff,
      Constants.ROUTE_BUFFER_KM,
      Constants.MAX_DETOUR_KM,
    );
    if (!result) continue;

    const existing = await db.query.matchesTable.findFirst({
      where: and(eq(matchesTable.tripId, trip.id), eq(matchesTable.parcelId, parcel.id)),
    });
    if (existing) continue;

    const [match] = await db
      .insert(matchesTable)
      .values({
        tripId: trip.id,
        parcelId: parcel.id,
        carrierId: trip.carrierId,
        senderId: parcel.senderId,
        score: String(Math.round(result.score * 100) / 100),
        alongRoute: result.isAlongRoute,
        detourKm: String(Math.round(result.detourKm * 100) / 100),
        carrierEarning: parcel.carrierEarning,
      })
      .returning();

    createdMatches.push(toMatchJson(match, { parcel, trip }));
  }

  ok(res, createdMatches, `${createdMatches.length} match(es) found`);
});

export default router;
