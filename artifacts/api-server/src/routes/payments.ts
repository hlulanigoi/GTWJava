import { Router, type IRouter } from "express";
import { z } from "zod";
import { eq } from "drizzle-orm";
import { db, paymentsTable } from "@workspace/db";
import { requireAuth } from "../middlewares/auth";
import { fail, ok } from "../lib/response";
import { toPaymentJson } from "../lib/serialize";
import { randomUUID } from "node:crypto";

const router: IRouter = Router();

const initiateSchema = z.object({
  amount: z.coerce.number().positive(),
  purpose: z.enum(["PARCEL", "TICKET"]),
});

/** Server-generates the bank-transfer reference (replaces the old client-side "GTW-" + timestamp). */
router.post("/payments/initiate", requireAuth, async (req, res) => {
  const parsed = initiateSchema.safeParse(req.body);
  if (!parsed.success) return fail(res, parsed.error.issues[0].message, 422);
  const { amount, purpose } = parsed.data;

  const reference = `GTW-${randomUUID().slice(0, 10).toUpperCase()}`;

  const [payment] = await db
    .insert(paymentsTable)
    .values({
      userId: req.user!.userId,
      reference,
      amount: String(amount),
      type: purpose,
    })
    .returning();

  ok(res, { reference: payment.reference }, "Payment initiated", 201);
});

/** Lets a user check the current status of a payment reference they submitted. */
router.post("/payments/verify", requireAuth, async (req, res) => {
  const schema = z.object({ reference: z.string().min(1) });
  const parsed = schema.safeParse(req.body);
  if (!parsed.success) return fail(res, parsed.error.issues[0].message, 422);

  const payment = await db.query.paymentsTable.findFirst({
    where: eq(paymentsTable.reference, parsed.data.reference),
  });
  if (!payment || payment.userId !== req.user!.userId) {
    return fail(res, "Payment reference not found", 404);
  }

  ok(res, toPaymentJson(payment));
});

export default router;
