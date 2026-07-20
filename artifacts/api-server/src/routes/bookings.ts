import { Router, type IRouter } from "express";
import { eq, or } from "drizzle-orm";
import { db, bookingsTable, ridesTable, tripsTable } from "@workspace/db";
import { requireAuth, requireApprovedDriver } from "../middlewares/auth";
import { fail, ok } from "../lib/response";
import { toBookingJson } from "../lib/serialize";

const router: IRouter = Router();

router.get("/bookings", requireAuth, async (req, res) => {
  const userId = req.user!.userId;
  const rows = await db.query.bookingsTable.findMany({
    where: or(eq(bookingsTable.driverId, userId), eq(bookingsTable.riderId, userId)),
  });

  const enriched = await Promise.all(
    rows.map(async (b) => {
      const [ride, trip] = await Promise.all([
        db.query.ridesTable.findFirst({ where: eq(ridesTable.id, b.rideId) }),
        db.query.tripsTable.findFirst({ where: eq(tripsTable.id, b.tripId) }),
      ]);
      return toBookingJson(b, { ride, trip });
    }),
  );

  ok(res, enriched);
});

async function requireBookingParty(req: any, res: any) {
  const booking = await db.query.bookingsTable.findFirst({ where: eq(bookingsTable.id, req.params.id as string) });
  if (!booking) {
    fail(res, "Booking not found", 404);
    return null;
  }
  if (booking.driverId !== req.user!.userId && booking.riderId !== req.user!.userId) {
    fail(res, "Not authorized for this booking", 403);
    return null;
  }
  return booking;
}

router.post("/bookings/:id/accept", requireAuth, requireApprovedDriver, async (req, res) => {
  const booking = await requireBookingParty(req, res);
  if (!booking) return;

  const [updated] = await db
    .update(bookingsTable)
    .set({ status: "ACCEPTED", acceptedAt: new Date() })
    .where(eq(bookingsTable.id, booking.id))
    .returning();

  await db.update(ridesTable).set({ status: "MATCHED", driverId: booking.driverId }).where(eq(ridesTable.id, booking.rideId));

  ok(res, toBookingJson(updated), "Booking accepted");
});

router.post("/bookings/:id/reject", requireAuth, requireApprovedDriver, async (req, res) => {
  const booking = await requireBookingParty(req, res);
  if (!booking) return;

  const [updated] = await db
    .update(bookingsTable)
    .set({ status: "REJECTED" })
    .where(eq(bookingsTable.id, booking.id))
    .returning();

  ok(res, toBookingJson(updated), "Booking rejected");
});

router.post("/bookings/:id/pickup", requireAuth, requireApprovedDriver, async (req, res) => {
  const booking = await requireBookingParty(req, res);
  if (!booking) return;
  if (booking.driverId !== req.user!.userId) return fail(res, "Only the driver can mark picked up", 403);

  const [updated] = await db
    .update(bookingsTable)
    .set({ status: "PICKED_UP", pickedUpAt: new Date() })
    .where(eq(bookingsTable.id, booking.id))
    .returning();

  await db.update(ridesTable).set({ status: "EN_ROUTE" }).where(eq(ridesTable.id, booking.rideId));

  ok(res, toBookingJson(updated), "Ride marked picked up");
});

router.post("/bookings/:id/complete", requireAuth, requireApprovedDriver, async (req, res) => {
  const booking = await requireBookingParty(req, res);
  if (!booking) return;
  if (booking.driverId !== req.user!.userId) return fail(res, "Only the driver can mark completed", 403);

  const [updated] = await db
    .update(bookingsTable)
    .set({ status: "COMPLETED", completedAt: new Date() })
    .where(eq(bookingsTable.id, booking.id))
    .returning();

  await db.update(ridesTable).set({ status: "COMPLETED" }).where(eq(ridesTable.id, booking.rideId));

  ok(res, toBookingJson(updated), "Ride marked completed");
});

export default router;
