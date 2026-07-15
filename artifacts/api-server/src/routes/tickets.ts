import { Router, type IRouter } from "express";
import { z } from "zod";
import { desc, eq } from "drizzle-orm";
import { db, paymentsTable, ticketsTable } from "@workspace/db";
import { requireAuth } from "../middlewares/auth";
import { fail, ok } from "../lib/response";
import { toTicketJson } from "../lib/serialize";
import { Constants } from "../lib/constants";
import { randomUUID } from "node:crypto";

const router: IRouter = Router();

router.get("/tickets/my", requireAuth, async (req, res) => {
  const rows = await db.query.ticketsTable.findMany({
    where: eq(ticketsTable.userId, req.user!.userId),
    orderBy: desc(ticketsTable.createdAt),
  });
  ok(res, rows.map((t) => toTicketJson(t)));
});

router.get("/tickets/price", requireAuth, async (_req, res) => {
  ok(res, { price: Constants.DEFAULT_TICKET_PRICE });
});

router.post("/tickets/purchase", requireAuth, async (req, res) => {
  const schema = z.object({ payment_reference: z.string().min(1) });
  const parsed = schema.safeParse(req.body);
  if (!parsed.success) return fail(res, parsed.error.issues[0].message, 422);

  const payment = await db.query.paymentsTable.findFirst({
    where: eq(paymentsTable.reference, parsed.data.payment_reference),
  });
  if (!payment || payment.userId !== req.user!.userId) {
    return fail(res, "Payment reference not found", 404);
  }
  if (payment.status !== "VERIFIED") {
    return fail(res, "Payment has not been verified yet", 409);
  }

  const code = `GTW-TKT-${randomUUID().slice(0, 8).toUpperCase()}`;
  const expiresAt = new Date();
  expiresAt.setDate(expiresAt.getDate() + 30);

  const [ticket] = await db
    .insert(ticketsTable)
    .values({
      userId: req.user!.userId,
      code,
      pricePaid: String(payment.amount),
      paymentReference: payment.reference,
    })
    .returning();

  await db.update(ticketsTable).set({ status: "ACTIVE", expiresAt }).where(eq(ticketsTable.id, ticket.id));

  ok(res, toTicketJson({ ...ticket, status: "ACTIVE", expiresAt }), "Ticket purchased", 201);
});

export default router;
