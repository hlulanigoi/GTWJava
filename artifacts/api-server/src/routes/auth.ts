import { Router, type IRouter } from "express";
import { z } from "zod";
import { eq } from "drizzle-orm";
import { db, usersTable } from "@workspace/db";
import { comparePassword, hashPassword, signToken } from "../lib/auth";
import { fail, ok } from "../lib/response";
import { requireAuth } from "../middlewares/auth";
import { toUserJson } from "../lib/serialize";

const router: IRouter = Router();

const registerSchema = z.object({
  full_name: z.string().min(1),
  email: z.string().email(),
  password: z.string().min(6),
  phone: z.string().optional(),
});

const loginSchema = z.object({
  email: z.string().email(),
  password: z.string().min(1),
});

router.post("/auth/register", async (req, res) => {
  const parsed = registerSchema.safeParse(req.body);
  if (!parsed.success) return fail(res, parsed.error.issues[0].message, 422);
  const { full_name, email, password, phone } = parsed.data;

  const existing = await db.query.usersTable.findFirst({
    where: eq(usersTable.email, email),
  });
  if (existing) return fail(res, "An account with this email already exists", 409);

  const passwordHash = await hashPassword(password);
  const [user] = await db
    .insert(usersTable)
    .values({ fullName: full_name, email, phone, passwordHash })
    .returning();

  const token = signToken({ userId: user.id, role: user.role });
  ok(res, { token, user: toUserJson(user) }, "Registered", 201);
});

router.post("/auth/login", async (req, res) => {
  const parsed = loginSchema.safeParse(req.body);
  if (!parsed.success) return fail(res, parsed.error.issues[0].message, 422);
  const { email, password } = parsed.data;

  const user = await db.query.usersTable.findFirst({
    where: eq(usersTable.email, email),
  });
  if (!user) return fail(res, "Invalid email or password", 401);

  const matches = await comparePassword(password, user.passwordHash);
  if (!matches) return fail(res, "Invalid email or password", 401);
  if (!user.isActive) return fail(res, "This account has been deactivated", 403);

  const token = signToken({ userId: user.id, role: user.role });
  ok(res, { token, user: toUserJson(user) }, "Logged in");
});

router.post("/auth/logout", requireAuth, async (_req, res) => {
  // Stateless JWT — client just discards the token.
  ok(res, null, "Logged out");
});

router.get("/auth/me", requireAuth, async (req, res) => {
  const user = await db.query.usersTable.findFirst({
    where: eq(usersTable.id, req.user!.userId),
  });
  if (!user) return fail(res, "User not found", 404);
  ok(res, toUserJson(user));
});

export default router;
