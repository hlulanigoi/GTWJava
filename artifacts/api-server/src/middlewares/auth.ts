import type { NextFunction, Request, Response } from "express";
import { verifyToken, type JwtPayload } from "../lib/auth";
import { fail } from "../lib/response";
import { db, usersTable } from "@workspace/db";
import { eq } from "drizzle-orm";

declare global {
  // eslint-disable-next-line @typescript-eslint/no-namespace
  namespace Express {
    interface Request {
      user?: JwtPayload;
    }
  }
}

export function requireAuth(req: Request, res: Response, next: NextFunction) {
  const header = req.headers.authorization;
  if (!header?.startsWith("Bearer ")) {
    return fail(res, "Missing or invalid Authorization header", 401);
  }

  try {
    req.user = verifyToken(header.slice("Bearer ".length));
    next();
  } catch {
    return fail(res, "Invalid or expired token", 401);
  }
}

export function requireAdmin(req: Request, res: Response, next: NextFunction) {
  if (req.user?.role !== "ADMIN") {
    return fail(res, "Admin access required", 403);
  }
  next();
}

export async function requireApprovedDriver(req: Request, res: Response, next: NextFunction) {
  if (!req.user) {
    return fail(res, "Authentication required", 401);
  }
  const user = await db.query.usersTable.findFirst({ where: eq(usersTable.id, req.user.userId) });
  if (!user || !user.isApprovedDriver) {
    return fail(res, "Only approved association drivers can do this", 403);
  }
  next();
}
