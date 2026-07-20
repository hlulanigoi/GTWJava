import { Router, type IRouter } from "express";
import { z } from "zod";
import { desc, eq, isNotNull } from "drizzle-orm";
import { db, ridesTable, paymentsTable, ticketsTable, tripsTable, usersTable } from "@workspace/db";
import { requireAdmin, requireAuth } from "../middlewares/auth";
import { fail, ok, paginated } from "../lib/response";
import { toRideJson, toPaymentJson, toUserJson } from "../lib/serialize";

const router: IRouter = Router();

router.use(requireAuth, requireAdmin);

router.get("/admin/stats", async (_req, res) => {
  const [users, rides, trips, tickets, payments] = await Promise.all([
    db.query.usersTable.findMany(),
    db.query.ridesTable.findMany(),
    db.query.tripsTable.findMany(),
    db.query.ticketsTable.findMany(),
    db.query.paymentsTable.findMany(),
  ]);

  const verifiedPayments = payments.filter((p) => p.status === "VERIFIED");
  const totalRevenue = verifiedPayments.reduce((sum, p) => sum + Number(p.amount), 0);
  const platformEarnings = rides
    .filter((r) => r.paymentVerified === "true")
    .reduce((sum, r) => sum + Number(r.platformFee), 0);

  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const completedToday = rides.filter(
    (r) => r.status === "COMPLETED" && new Date(r.updatedAt) >= today,
  ).length;

  const revenueThisWeek: number[] = [];
  const weekLabels: string[] = [];
  for (let i = 6; i >= 0; i--) {
    const day = new Date();
    day.setHours(0, 0, 0, 0);
    day.setDate(day.getDate() - i);
    const nextDay = new Date(day);
    nextDay.setDate(nextDay.getDate() + 1);

    const dayRevenue = verifiedPayments
      .filter((p) => p.verifiedAt && new Date(p.verifiedAt) >= day && new Date(p.verifiedAt) < nextDay)
      .reduce((sum, p) => sum + Number(p.amount), 0);

    revenueThisWeek.push(Math.round(dayRevenue * 100) / 100);
    weekLabels.push(day.toLocaleDateString("en-US", { weekday: "short" }));
  }

  ok(res, {
    total_users: users.length,
    total_rides: rides.length,
    total_trips: trips.length,
    total_tickets_sold: tickets.length,
    total_revenue: Math.round(totalRevenue * 100) / 100,
    platform_earnings: Math.round(platformEarnings * 100) / 100,
    pending_rides: rides.filter((r) => r.status === "PENDING").length,
    active_trips: trips.filter((t) => t.status === "ACTIVE").length,
    pending_payments: payments.filter((p) => p.status === "PENDING").length,
    completed_today: completedToday,
    revenue_this_week: revenueThisWeek,
    week_labels: weekLabels,
  });
});

router.get("/admin/users", async (req, res) => {
  const page = Math.max(1, Number(req.query.page) || 1);
  const perPage = 20;
  const rows = await db.query.usersTable.findMany({ orderBy: desc(usersTable.createdAt) });
  const total = rows.length;
  const pageRows = rows.slice((page - 1) * perPage, page * perPage);
  paginated(res, pageRows.map((u) => toUserJson(u)), total, page, perPage);
});

router.patch("/admin/users/:id/activate", async (req, res) => {
  const [updated] = await db.update(usersTable).set({ isActive: true }).where(eq(usersTable.id, req.params.id as string)).returning();
  if (!updated) return fail(res, "User not found", 404);
  ok(res, toUserJson(updated), "User activated");
});

router.patch("/admin/users/:id/deactivate", async (req, res) => {
  const [updated] = await db.update(usersTable).set({ isActive: false }).where(eq(usersTable.id, req.params.id as string)).returning();
  if (!updated) return fail(res, "User not found", 404);
  ok(res, toUserJson(updated), "User deactivated");
});

router.get("/admin/rides", async (req, res) => {
  const page = Math.max(1, Number(req.query.page) || 1);
  const perPage = 20;
  const rows = await db.query.ridesTable.findMany({ orderBy: desc(ridesTable.createdAt) });
  const total = rows.length;
  const pageRows = rows.slice((page - 1) * perPage, page * perPage);
  paginated(res, pageRows.map((r) => toRideJson(r)), total, page, perPage);
});

router.patch("/admin/rides/:id/status", async (req, res) => {
  const schema = z.object({
    status: z.enum(["PENDING", "MATCHED", "EN_ROUTE", "COMPLETED", "CANCELLED"]),
  });
  const parsed = schema.safeParse(req.body);
  if (!parsed.success) return fail(res, parsed.error.issues[0].message, 422);

  const [updated] = await db
    .update(ridesTable)
    .set({ status: parsed.data.status })
    .where(eq(ridesTable.id, req.params.id as string))
    .returning();
  if (!updated) return fail(res, "Ride not found", 404);
  ok(res, toRideJson(updated), "Status updated");
});

router.get("/admin/drivers/pending", async (_req, res) => {
  const rows = await db.query.usersTable.findMany({
    where: isNotNull(usersTable.licenseNumber),
  });
  const pending = rows.filter((u) => !u.isApprovedDriver && u.licenseNumber !== null);
  ok(res, pending.map((u) => toUserJson(u)));
});

router.patch("/admin/drivers/:id/approve", async (req, res) => {
  const [updated] = await db
    .update(usersTable)
    .set({ isApprovedDriver: true })
    .where(eq(usersTable.id, req.params.id as string))
    .returning();
  if (!updated) return fail(res, "User not found", 404);
  ok(res, toUserJson(updated), "Driver approved");
});

router.patch("/admin/drivers/:id/reject", async (req, res) => {
  const [updated] = await db
    .update(usersTable)
    .set({ isApprovedDriver: false })
    .where(eq(usersTable.id, req.params.id as string))
    .returning();
  if (!updated) return fail(res, "User not found", 404);
  ok(res, toUserJson(updated), "Driver application rejected");
});

router.get("/admin/payments/pending", async (_req, res) => {
  const rows = await db.query.paymentsTable.findMany({
    where: eq(paymentsTable.status, "PENDING"),
    orderBy: desc(paymentsTable.createdAt),
  });
  ok(res, rows.map((p) => toPaymentJson(p)));
});

router.patch("/admin/payments/:ref/verify", async (req, res) => {
  const payment = await db.query.paymentsTable.findFirst({ where: eq(paymentsTable.reference, req.params.ref as string) });
  if (!payment) return fail(res, "Payment not found", 404);

  const [updated] = await db
    .update(paymentsTable)
    .set({ status: "VERIFIED", verifiedBy: req.user!.userId, verifiedAt: new Date() })
    .where(eq(paymentsTable.reference, req.params.ref as string))
    .returning();

  if (payment.type === "RIDE") {
    await db
      .update(ridesTable)
      .set({ paymentVerified: "true" })
      .where(eq(ridesTable.paymentReference, req.params.ref as string));
  }

  ok(res, toPaymentJson(updated), "Payment verified");
});

router.patch("/admin/payments/:ref/reject", async (req, res) => {
  const schema = z.object({ reason: z.string().optional() });
  const parsed = schema.safeParse(req.body);
  if (!parsed.success) return fail(res, parsed.error.issues[0].message, 422);

  const [updated] = await db
    .update(paymentsTable)
    .set({
      status: "REJECTED",
      verifiedBy: req.user!.userId,
      verifiedAt: new Date(),
      rejectionReason: parsed.data.reason,
    })
    .where(eq(paymentsTable.reference, req.params.ref as string))
    .returning();
  if (!updated) return fail(res, "Payment not found", 404);

  ok(res, toPaymentJson(updated), "Payment rejected");
});

export default router;
