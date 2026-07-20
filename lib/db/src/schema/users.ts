import {
  pgTable,
  text,
  uuid,
  boolean,
  numeric,
  integer,
  timestamp,
  pgEnum,
} from "drizzle-orm/pg-core";
import { createInsertSchema } from "drizzle-zod";
import { z } from "zod/v4";

export const userRoleEnum = pgEnum("user_role", ["USER", "ADMIN"]);

export const usersTable = pgTable("users", {
  id: uuid("id").primaryKey().defaultRandom(),
  fullName: text("full_name").notNull(),
  email: text("email").notNull().unique(),
  phone: text("phone"),
  passwordHash: text("password_hash").notNull(),
  role: userRoleEnum("role").notNull().default("USER"),
  rating: numeric("rating", { precision: 3, scale: 2 }).notNull().default("5.00"),
  totalRidesDriven: integer("total_rides_driven").notNull().default(0),
  totalRidesTaken: integer("total_rides_taken").notNull().default(0),
  ticketsOwned: integer("tickets_owned").notNull().default(0),
  isActive: boolean("is_active").notNull().default(true),
  isApprovedDriver: boolean("is_approved_driver").notNull().default(false),
  licenseNumber: text("license_number"),
  vehiclePlate: text("vehicle_plate"),
  vehicleModel: text("vehicle_model"),
  createdAt: timestamp("created_at", { withTimezone: true }).notNull().defaultNow(),
  updatedAt: timestamp("updated_at", { withTimezone: true }).notNull().defaultNow().$onUpdate(() => new Date()),
});

export const insertUserSchema = createInsertSchema(usersTable).omit({
  id: true,
  createdAt: true,
  updatedAt: true,
  totalRidesDriven: true,
  totalRidesTaken: true,
  ticketsOwned: true,
  isActive: true,
  isApprovedDriver: true,
});

export type InsertUser = z.infer<typeof insertUserSchema>;
export type User = typeof usersTable.$inferSelect;
