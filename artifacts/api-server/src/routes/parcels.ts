import { Router, type IRouter } from "express";
import { z } from "zod";
import { and, desc, eq, sql } from "drizzle-orm";
import { db, parcelsTable, usersTable } from "@workspace/db";
import { requireAuth } from "../middlewares/auth";
import { fail, ok, paginated } from "../lib/response";
import { toParcelJson } from "../lib/serialize";
import { geocodeAddress } from "../lib/geocode";
import { haversineKm } from "../lib/haversine";

const router: IRouter = Router();

const createParcelSchema = z.object({
  description: z.string().min(1),
  weight_kg: z.coerce.number().positive(),
  size_label: z.enum(["SMALL", "MEDIUM", "LARGE"]).default("SMALL"),
  pickup_address: z.string().min(1),
  destination_address: z.string().min(1),
  special_instructions: z.string().optional(),
  payment_reference: z.string().min(1),
});

function calculateFee(weightKg: number) {
  const fee = Math.round((30 + weightKg * 10) * 100) / 100;
  const carrierEarning = Math.round(fee * 0.8 * 100) / 100;
  const platformFee = Math.round((fee - carrierEarning) * 100) / 100;
  return { fee, carrierEarning, platformFee };
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
  paginated(res, pageRows.map((p) => toParcelJson(p)), total, page, perPage);
});

router.get("/parcels/my", requireAuth, async (req, res) => {
  const rows = await db.query.parcelsTable.findMany({
    where: eq(parcelsTable.senderId, req.user!.userId),
    orderBy: desc(parcelsTable.createdAt),
  });
  ok(res, rows.map((p) => toParcelJson(p)));
});

router.get("/parcels/:id", requireAuth, async (req, res) => {
  const parcel = await db.query.parcelsTable.findFirst({
    where: eq(parcelsTable.id, (req.params.id as string)),
  });
  if (!parcel) return fail(res, "Parcel not found", 404);

  const [sender, carrier] = await Promise.all([
    db.query.usersTable.findFirst({ where: eq(usersTable.id, parcel.senderId) }),
    parcel.carrierId
      ? db.query.usersTable.findFirst({ where: eq(usersTable.id, parcel.carrierId) })
      : Promise.resolve(null),
  ]);

  ok(res, toParcelJson(parcel, { sender, carrier }));
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

  const { fee, carrierEarning, platformFee } = calculateFee(data.weight_kg);

  const [parcel] = await db
    .insert(parcelsTable)
    .values({
      senderId: req.user!.userId,
      description: data.description,
      weight: String(data.weight_kg),
      size: data.size_label,
      pickupAddress: data.pickup_address,
      pickupLat: String(pickup.lat),
      pickupLng: String(pickup.lng),
      destinationAddress: data.destination_address,
      destinationLat: String(destination.lat),
      destinationLng: String(destination.lng),
      fee: String(fee),
      carrierEarning: String(carrierEarning),
      platformFee: String(platformFee),
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
    where: eq(parcelsTable.id, (req.params.id as string)),
  });
  if (!parcel) return fail(res, "Parcel not found", 404);
  if (parcel.senderId !== req.user!.userId && parcel.carrierId !== req.user!.userId && req.user!.role !== "ADMIN") {
    return fail(res, "Not authorized to update this parcel", 403);
  }

  const [updated] = await db
    .update(parcelsTable)
    .set({ status: parsed.data.status })
    .where(eq(parcelsTable.id, (req.params.id as string)))
    .returning();

  ok(res, toParcelJson(updated), "Status updated");
});

router.delete("/parcels/:id", requireAuth, async (req, res) => {
  const parcel = await db.query.parcelsTable.findFirst({
    where: eq(parcelsTable.id, (req.params.id as string)),
  });
  if (!parcel) return fail(res, "Parcel not found", 404);
  if (parcel.senderId !== req.user!.userId && req.user!.role !== "ADMIN") {
    return fail(res, "Not authorized to delete this parcel", 403);
  }
  if (parcel.status !== "PENDING") {
    return fail(res, "Only pending parcels can be deleted", 409);
  }

  await db.delete(parcelsTable).where(eq(parcelsTable.id, (req.params.id as string)));
  ok(res, null, "Parcel deleted");
});

export default router;
