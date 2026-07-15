import { Router, type IRouter } from "express";
import { eq, or } from "drizzle-orm";
import { db, matchesTable, parcelsTable, tripsTable } from "@workspace/db";
import { requireAuth } from "../middlewares/auth";
import { fail, ok } from "../lib/response";
import { toMatchJson } from "../lib/serialize";

const router: IRouter = Router();

router.get("/matches", requireAuth, async (req, res) => {
  const userId = req.user!.userId;
  const rows = await db.query.matchesTable.findMany({
    where: or(eq(matchesTable.carrierId, userId), eq(matchesTable.senderId, userId)),
  });

  const enriched = await Promise.all(
    rows.map(async (m) => {
      const [parcel, trip] = await Promise.all([
        db.query.parcelsTable.findFirst({ where: eq(parcelsTable.id, m.parcelId) }),
        db.query.tripsTable.findFirst({ where: eq(tripsTable.id, m.tripId) }),
      ]);
      return toMatchJson(m, { parcel, trip });
    }),
  );

  ok(res, enriched);
});

async function requireMatchParty(req: any, res: any) {
  const match = await db.query.matchesTable.findFirst({ where: eq(matchesTable.id, (req.params.id as string)) });
  if (!match) {
    fail(res, "Match not found", 404);
    return null;
  }
  if (match.carrierId !== req.user!.userId && match.senderId !== req.user!.userId) {
    fail(res, "Not authorized for this match", 403);
    return null;
  }
  return match;
}

router.post("/matches/:id/accept", requireAuth, async (req, res) => {
  const match = await requireMatchParty(req, res);
  if (!match) return;

  const [updated] = await db
    .update(matchesTable)
    .set({ status: "ACCEPTED", acceptedAt: new Date() })
    .where(eq(matchesTable.id, match.id))
    .returning();

  await db.update(parcelsTable).set({ status: "MATCHED", carrierId: match.carrierId }).where(eq(parcelsTable.id, match.parcelId));

  ok(res, toMatchJson(updated), "Match accepted");
});

router.post("/matches/:id/reject", requireAuth, async (req, res) => {
  const match = await requireMatchParty(req, res);
  if (!match) return;

  const [updated] = await db
    .update(matchesTable)
    .set({ status: "REJECTED" })
    .where(eq(matchesTable.id, match.id))
    .returning();

  ok(res, toMatchJson(updated), "Match rejected");
});

router.post("/matches/:id/collect", requireAuth, async (req, res) => {
  const match = await requireMatchParty(req, res);
  if (!match) return;
  if (match.carrierId !== req.user!.userId) return fail(res, "Only the carrier can mark collected", 403);

  const [updated] = await db
    .update(matchesTable)
    .set({ status: "COLLECTED", collectedAt: new Date() })
    .where(eq(matchesTable.id, match.id))
    .returning();

  await db.update(parcelsTable).set({ status: "COLLECTED" }).where(eq(parcelsTable.id, match.parcelId));

  ok(res, toMatchJson(updated), "Parcel marked collected");
});

router.post("/matches/:id/deliver", requireAuth, async (req, res) => {
  const match = await requireMatchParty(req, res);
  if (!match) return;
  if (match.carrierId !== req.user!.userId) return fail(res, "Only the carrier can mark delivered", 403);

  const [updated] = await db
    .update(matchesTable)
    .set({ status: "DELIVERED", deliveredAt: new Date() })
    .where(eq(matchesTable.id, match.id))
    .returning();

  await db.update(parcelsTable).set({ status: "DELIVERED" }).where(eq(parcelsTable.id, match.parcelId));

  ok(res, toMatchJson(updated), "Parcel marked delivered");
});

export default router;
