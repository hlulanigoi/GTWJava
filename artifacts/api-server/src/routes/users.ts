import { Router, type IRouter } from "express";
import { z } from "zod";
import { eq } from "drizzle-orm";
import { db, usersTable } from "@workspace/db";
import { requireAuth } from "../middlewares/auth";
import { fail, ok } from "../lib/response";
import { toUserJson } from "../lib/serialize";

const router: IRouter = Router();

router.get("/users/:id", requireAuth, async (req, res) => {
  const user = await db.query.usersTable.findFirst({ where: eq(usersTable.id, req.params.id as string) });
  if (!user) return fail(res, "User not found", 404);
  ok(res, toUserJson(user));
});

router.patch("/users/profile", requireAuth, async (req, res) => {
  const schema = z.object({
    full_name: z.string().min(1).optional(),
    phone: z.string().optional(),
  });
  const parsed = schema.safeParse(req.body);
  if (!parsed.success) return fail(res, parsed.error.issues[0].message, 422);

  const [updated] = await db
    .update(usersTable)
    .set({
      ...(parsed.data.full_name ? { fullName: parsed.data.full_name } : {}),
      ...(parsed.data.phone ? { phone: parsed.data.phone } : {}),
    })
    .where(eq(usersTable.id, req.user!.userId))
    .returning();

  ok(res, toUserJson(updated), "Profile updated");
});

router.post("/users/me/apply-driver", requireAuth, async (req, res) => {
  const schema = z.object({
    license_number: z.string().min(1),
    vehicle_plate: z.string().min(1),
    vehicle_model: z.string().min(1),
  });
  const parsed = schema.safeParse(req.body);
  if (!parsed.success) return fail(res, parsed.error.issues[0].message, 422);

  const [updated] = await db
    .update(usersTable)
    .set({
      licenseNumber: parsed.data.license_number,
      vehiclePlate: parsed.data.vehicle_plate,
      vehicleModel: parsed.data.vehicle_model,
      isApprovedDriver: false,
    })
    .where(eq(usersTable.id, req.user!.userId))
    .returning();

  ok(res, toUserJson(updated), "Driver application submitted. Awaiting admin approval.");
});

export default router;
