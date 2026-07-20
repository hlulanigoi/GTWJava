import { Router, type IRouter } from "express";
import { z } from "zod";
import { and, desc, eq } from "drizzle-orm";
import { db, matchesTable, parcelsTable, tripsTable } from "@workspace/db";
import { requireAuth } from "../middlewares/auth";
import { fail, ok, paginated } from "../lib/response";
import { toMatchJson, toParcelJson } from "../lib/serialize";
import { geocodeAddress } from "../lib/geocode";
import { haversineKm, matchRideToRoute } from "../lib/haversine";
import { Constants } from "../lib/constants";

const router: IRouter = Router();

const createParcelSchema = z.object({
  description: z.string().min(1),
  weight: z.coerce.number().positive(),
  size: z.enum(["SMALL", "MEDIUM", "LARGE"]).default("SMALL"),
  pickup_address: z.string().min(1),
  destination_address: z.string().min(1),
  special_instructions: z.string().optional(),
  payment_reference: z.string().min(1),
});

const SIZE_BASE: Record<string, number> = { SMALL: 20, MEDIUM: 35, LARGE: 50 };

function calculateFee(weight: number, size: string) {
  const base = SIZE_BASE[size] ?? 20;
  const fee = Math.round((base + weight * 5) * 100) / 100;
  const platformFee = Math.round(fee * Constants.PLATFORM_FEE_PERCENT * 100) / 100;
  const carrierEarning = Math.round((fee - platformFee) * 100) / 100;
  return { fee, platformFee, carrierEarning };
}

router.get("/parcels", requireAuth, async (req, res) => {
  const page = Math.max(1, Number(req.query.page) || 1);
  const perPage = 20;
  const status = (req.query.status as string) || "PENDING";
  const lat = req.query.lat !== undefined ? Number(req.query.lat) : undefined;
  const lng = req.query.lng !== undefined ? Number(req.query.lng) : undefined;
  const radiusKm = req.query.radius_km !== undefined ? Number(req.query.radius_km) : undefined;

  const rows = await db.query.parcelsTable.findMany({
    where: eq(parcelsTable.status, status as any),
    orderBy: desc(parcelsTable.createdAt),
  });

  const filtered =
    lat !== undefined && lng !== undefined && radiusKm !== undefined
      ? rows.filter(
          (p) =>
            haversineKm({ lat, lng }, { lat: Number(p.pickupLat), lng: Number(p.pickupLng) }) <=
            radiusKm,
        )
      : rows;

  const total = filtered.length;
  const pageRows = filtered.slice((page - 1) * perPage, page * perPage);
  paginated(res, pageRows.map(toParcelJson), total, page, perPage);
});

router.get("/parcels/my", requireAuth, async (req, res) => {
  const rows = await db.query.parcelsTable.findMany({
    where: eq(parcelsTable.senderId, req.user!.userId),
    orderBy: desc(parcelsTable.createdAt),
  });
  ok(res, rows.map(toParcelJson));
});

router.get("/parcels/:id", requireAuth, async (req, res) => {
  const parcel = await db.query.parcelsTable.findFirst({
    where: eq(parcelsTable.id, req.params.id as string),
  });
  if (!parcel) return fail(res, "Parcel not found", 404);
  ok(res, toParcelJson(parcel));
});

router.post("/parcels", requireAuth, async (req, res) => {
  const parsed = createParcelSchema.safeParse(req.body);
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

  const { fee, platformFee, carrierEarning } = calculateFee(data.weight, data.size);

  const [parcel] = await db
    .insert(parcelsTable)
    .values({
      senderId: req.user!.userId,
      description: data.description,
      weight: String(data.weight),
      size: data.size,
      pickupAddress: data.pickup_address,
      pickupLat: String(pickup.lat),
      pickupLng: String(pickup.lng),
      destinationAddress: data.destination_address,
      destinationLat: String(destination.lat),
      destinationLng: String(destination.lng),
      fee: String(fee),
      platformFee: String(platformFee),
      carrierEarning: String(carrierEarning),
      paymentReference: data.payment_reference,
      specialInstructions: data.special_instructions,
    })
    .returning();

  ok(res, toParcelJson(parcel), "Parcel created", 201);
});

router.patch("/parcels/:id/status", requireAuth, async (req, res) => {
  const schema = z.object({
    status: z.enum(["PENDING", "MATCHED", "COLLECTED", "DELIVERED", "CANCELLED"]),
  });
  const parsed = schema.safeParse(req.body);
  if (!parsed.success) return fail(res, parsed.error.issues[0].message, 422);

  const parcel = await db.query.parcelsTable.findFirst({
    where: eq(parcelsTable.id, req.params.id as string),
  });
  if (!parcel) return fail(res, "Parcel not found", 404);
  if (parcel.senderId !== req.user!.userId && req.user!.role !== "ADMIN") {
    return fail(res, "Not authorized to update this parcel", 403);
  }

  const [updated] = await db
    .update(parcelsTable)
    .set({ status: parsed.data.status })
    .where(eq(parcelsTable.id, req.params.id as string))
    .returning();

  ok(res, toParcelJson(updated), "Status updated");
});

router.delete("/parcels/:id", requireAuth, async (req, res) => {
  const parcel = await db.query.parcelsTable.findFirst({
    where: eq(parcelsTable.id, req.params.id as string),
  });
  if (!parcel) return fail(res, "Parcel not found", 404);
  if (parcel.senderId !== req.user!.userId && req.user!.role !== "ADMIN") {
    return fail(res, "Not authorized to delete this parcel", 403);
  }
  if (parcel.status !== "PENDING") {
    return fail(res, "Only pending parcels can be deleted", 409);
  }

  await db.delete(parcelsTable).where(eq(parcelsTable.id, req.params.id as string));
  ok(res, null, "Parcel deleted");
});

/**
 * Match all PENDING parcels against this trip's route using the Haversine corridor algorithm.
 * Creates a Match record for each parcel that fits within the route buffer.
 */
router.post("/trips/:id/match-parcels", requireAuth, async (req, res) => {
  const trip = await db.query.tripsTable.findFirst({
    where: eq(tripsTable.id, req.params.id as string),
  });
  if (!trip) return fail(res, "Trip not found", 404);
  if (trip.driverId !== req.user!.userId && req.user!.role !== "ADMIN") {
    return fail(res, "Not authorized to match this trip", 403);
  }

  const pendingParcels = await db.query.parcelsTable.findMany({
    where: eq(parcelsTable.status, "PENDING"),
  });

  const origin = { lat: Number(trip.originLat), lng: Number(trip.originLng) };
  const destination = { lat: Number(trip.destinationLat), lng: Number(trip.destinationLng) };

  const created = [];
  for (const parcel of pendingParcels) {
    const pickup = { lat: Number(parcel.pickupLat), lng: Number(parcel.pickupLng) };
    const dropoff = { lat: Number(parcel.destinationLat), lng: Number(parcel.destinationLng) };

    const result = matchRideToRoute(
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
        carrierId: trip.driverId,
        senderId: parcel.senderId,
        score: String(Math.round(result.score * 100) / 100),
        alongRoute: result.isAlongRoute,
        detourKm: String(Math.round(result.detourKm * 100) / 100),
        carrierEarning: parcel.carrierEarning,
      })
      .returning();

    created.push(toMatchJson(match, { parcel, trip }));
  }

  ok(res, created, `${created.length} match(es) found`);
});

export default router;
