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

export const parcelStatusEnum = pgEnum("parcel_status", [
  "PENDING",
  "MATCHED",
  "COLLECTED",
  "DELIVERED",
  "CANCELLED",
]);

export const parcelSizeEnum = pgEnum("parcel_size", ["SMALL", "MEDIUM", "LARGE"]);

export const parcelsTable = pgTable("parcels", {
  id: uuid("id").primaryKey().defaultRandom(),
  senderId: uuid("sender_id").notNull().references(() => usersTable.id),
  carrierId: uuid("carrier_id").references(() => usersTable.id),

  description: text("description").notNull(),
  weight: numeric("weight", { precision: 6, scale: 2 }).notNull(),
  size: parcelSizeEnum("size").notNull().default("SMALL"),

  pickupAddress: text("pickup_address").notNull(),
  pickupLat: numeric("pickup_lat", { precision: 10, scale: 7 }).notNull().default("0"),
  pickupLng: numeric("pickup_lng", { precision: 10, scale: 7 }).notNull().default("0"),

  destinationAddress: text("destination_address").notNull(),
  destinationLat: numeric("destination_lat", { precision: 10, scale: 7 }).notNull().default("0"),
  destinationLng: numeric("destination_lng", { precision: 10, scale: 7 }).notNull().default("0"),

  status: parcelStatusEnum("status").notNull().default("PENDING"),
  fee: numeric("fee", { precision: 10, scale: 2 }).notNull(),
  platformFee: numeric("platform_fee", { precision: 10, scale: 2 }).notNull(),
  carrierEarning: numeric("carrier_earning", { precision: 10, scale: 2 }).notNull(),
  paymentReference: text("payment_reference").notNull(),
  paymentVerified: text("payment_verified").notNull().default("false"),

  specialInstructions: text("special_instructions"),

  createdAt: timestamp("created_at", { withTimezone: true }).notNull().defaultNow(),
  updatedAt: timestamp("updated_at", { withTimezone: true }).notNull().defaultNow().$onUpdate(() => new Date()),
});

export const insertParcelSchema = createInsertSchema(parcelsTable).omit({
  id: true,
  carrierId: true,
  status: true,
  paymentVerified: true,
  createdAt: true,
  updatedAt: true,
});

export type InsertParcel = z.infer<typeof insertParcelSchema>;
export type Parcel = typeof parcelsTable.$inferSelect;
