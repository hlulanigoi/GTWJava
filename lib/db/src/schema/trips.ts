import {
  pgTable,
  text,
  uuid,
  numeric,
  timestamp,
  pgEnum,
} from "drizzle-orm/pg-core";
import { createInsertSchema } from "drizzle-zod";
import { z } from "zod/v4";
import { usersTable } from "./users";

export const tripStatusEnum = pgEnum("trip_status", [
  "ACTIVE",
  "COMPLETED",
  "CANCELLED",
]);

export const transportModeEnum = pgEnum("transport_mode", [
  "CAR",
  "BUS",
  "TRAIN",
  "WALK",
  "OTHER",
]);

export const tripsTable = pgTable("trips", {
  id: uuid("id").primaryKey().defaultRandom(),
  carrierId: uuid("carrier_id").notNull().references(() => usersTable.id),

  originAddress: text("origin_address").notNull(),
  originLat: numeric("origin_lat", { precision: 10, scale: 7 }).notNull().default("0"),
  originLng: numeric("origin_lng", { precision: 10, scale: 7 }).notNull().default("0"),

  destinationAddress: text("destination_address").notNull(),
  destinationLat: numeric("destination_lat", { precision: 10, scale: 7 }).notNull().default("0"),
  destinationLng: numeric("destination_lng", { precision: 10, scale: 7 }).notNull().default("0"),

  departureTime: timestamp("departure_time", { withTimezone: true }).notNull(),
  arrivalTime: timestamp("arrival_time", { withTimezone: true }),

  transportMode: transportModeEnum("transport_mode").notNull().default("CAR"),
  capacityKg: numeric("capacity_kg", { precision: 6, scale: 2 }).notNull(),
  notes: text("notes"),

  status: tripStatusEnum("status").notNull().default("ACTIVE"),

  createdAt: timestamp("created_at", { withTimezone: true }).notNull().defaultNow(),
  updatedAt: timestamp("updated_at", { withTimezone: true }).notNull().defaultNow().$onUpdate(() => new Date()),
});

export const insertTripSchema = createInsertSchema(tripsTable).omit({
  id: true,
  status: true,
  createdAt: true,
  updatedAt: true,
});

export type InsertTrip = z.infer<typeof insertTripSchema>;
export type Trip = typeof tripsTable.$inferSelect;
