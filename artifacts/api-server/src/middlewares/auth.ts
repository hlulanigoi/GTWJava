import type { NextFunction, Request, Response } from "express";
import { verifyToken, type JwtPayload } from "../lib/auth";
import { fail } from "../lib/response";

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
