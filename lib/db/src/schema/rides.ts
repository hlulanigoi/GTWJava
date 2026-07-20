import {
  pgTable,
  text,
  uuid,
  numeric,
  integer,
  timestamp,
  pgEnum,
} from "drizzle-orm/pg-core";
import { createInsertSchema } from "drizzle-zod";
import { z } from "zod/v4";
import { usersTable } from "./users";

export const rideStatusEnum = pgEnum("ride_status", [
  "PENDING",
  "MATCHED",
  "EN_ROUTE",
  "COMPLETED",
  "CANCELLED",
]);

export const luggageSizeEnum = pgEnum("luggage_size", ["NONE", "SMALL", "MEDIUM", "LARGE"]);

export const rideTypeEnum = pgEnum("ride_type", ["SCHEDULED", "ON_DEMAND"]);

export const ridesTable = pgTable("rides", {
  id: uuid("id").primaryKey().defaultRandom(),
  riderId: uuid("rider_id").notNull().references(() => usersTable.id),
  driverId: uuid("driver_id").references(() => usersTable.id),

  rideType: rideTypeEnum("ride_type").notNull().default("ON_DEMAND"),

  notes: text("notes"),
  passengerCount: integer("passenger_count").notNull().default(1),
  luggageSize: luggageSizeEnum("luggage_size").notNull().default("NONE"),

  pickupAddress: text("pickup_address").notNull(),
  pickupLat: numeric("pickup_lat", { precision: 10, scale: 7 }).notNull().default("0"),
  pickupLng: numeric("pickup_lng", { precision: 10, scale: 7 }).notNull().default("0"),

  destinationAddress: text("destination_address").notNull(),
  destinationLat: numeric("destination_lat", { precision: 10, scale: 7 }).notNull().default("0"),
  destinationLng: numeric("destination_lng", { precision: 10, scale: 7 }).notNull().default("0"),

  status: rideStatusEnum("status").notNull().default("PENDING"),
  fare: numeric("fare", { precision: 10, scale: 2 }).notNull(),
  platformFee: numeric("platform_fee", { precision: 10, scale: 2 }).notNull(),
  driverEarning: numeric("driver_earning", { precision: 10, scale: 2 }).notNull(),
  paymentReference: text("payment_reference").notNull(),
  paymentVerified: text("payment_verified").notNull().default("false"),

  createdAt: timestamp("created_at", { withTimezone: true }).notNull().defaultNow(),
  updatedAt: timestamp("updated_at", { withTimezone: true }).notNull().defaultNow().$onUpdate(() => new Date()),
});

export const insertRideSchema = createInsertSchema(ridesTable).omit({
  id: true,
  driverId: true,
  status: true,
  paymentVerified: true,
  createdAt: true,
  updatedAt: true,
});

export type InsertRide = z.infer<typeof insertRideSchema>;
export type Ride = typeof ridesTable.$inferSelect;
