import jwt from "jsonwebtoken";
import bcrypt from "bcryptjs";

const SECRET = process.env.SESSION_SECRET;

if (!SECRET) {
  throw new Error(
    "SESSION_SECRET must be set. Add it via the environment-secrets tool.",
  );
}

export interface JwtPayload {
  userId: string;
  role: "USER" | "ADMIN";
}

export function signToken(payload: JwtPayload): string {
  return jwt.sign(payload, SECRET as string, { expiresIn: "30d" });
}

export function verifyToken(token: string): JwtPayload {
  return jwt.verify(token, SECRET as string) as JwtPayload;
}

export function hashPassword(password: string): Promise<string> {
  return bcrypt.hash(password, 10);
}

export function comparePassword(
  password: string,
  hash: string,
): Promise<boolean> {
  return bcrypt.compare(password, hash);
}
