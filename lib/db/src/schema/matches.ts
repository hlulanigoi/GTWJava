import {
  pgTable,
  text,
  uuid,
  numeric,
  boolean,
  timestamp,
  pgEnum,
} from "drizzle-orm/pg-core";
import { createInsertSchema } from "drizzle-zod";
import { z } from "zod/v4";
import { usersTable } from "./users";
import { parcelsTable } from "./parcels";
import { tripsTable } from "./trips";

export const matchStatusEnum = pgEnum("match_status", [
  "PROPOSED",
  "ACCEPTED",
  "REJECTED",
  "COLLECTED",
  "DELIVERED",
]);

export const matchesTable = pgTable("matches", {
  id: uuid("id").primaryKey().defaultRandom(),

  tripId: uuid("trip_id").notNull().references(() => tripsTable.id),
  parcelId: uuid("parcel_id").notNull().references(() => parcelsTable.id),
  carrierId: uuid("carrier_id").notNull().references(() => usersTable.id),
  senderId: uuid("sender_id").notNull().references(() => usersTable.id),

  score: numeric("score", { precision: 5, scale: 2 }).notNull().default("0"),
  alongRoute: boolean("along_route").notNull().default(false),
  detourKm: numeric("detour_km", { precision: 6, scale: 2 }),

  status: matchStatusEnum("status").notNull().default("PROPOSED"),

  carrierEarning: numeric("carrier_earning", { precision: 10, scale: 2 }),

  acceptedAt: timestamp("accepted_at", { withTimezone: true }),
  collectedAt: timestamp("collected_at", { withTimezone: true }),
  deliveredAt: timestamp("delivered_at", { withTimezone: true }),

  createdAt: timestamp("created_at", { withTimezone: true }).notNull().defaultNow(),
  updatedAt: timestamp("updated_at", { withTimezone: true }).notNull().defaultNow().$onUpdate(() => new Date()),
});

export const insertMatchSchema = createInsertSchema(matchesTable).omit({
  id: true,
  status: true,
  acceptedAt: true,
  collectedAt: true,
  deliveredAt: true,
  createdAt: true,
  updatedAt: true,
});

export type InsertMatch = z.infer<typeof insertMatchSchema>;
export type Match = typeof matchesTable.$inferSelect;
