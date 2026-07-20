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
import { ridesTable } from "./rides";
import { tripsTable } from "./trips";

export const bookingStatusEnum = pgEnum("booking_status", [
  "PROPOSED",
  "ACCEPTED",
  "REJECTED",
  "PICKED_UP",
  "COMPLETED",
]);

export const bookingsTable = pgTable("bookings", {
  id: uuid("id").primaryKey().defaultRandom(),

  tripId: uuid("trip_id").notNull().references(() => tripsTable.id),
  rideId: uuid("ride_id").notNull().references(() => ridesTable.id),
  driverId: uuid("driver_id").notNull().references(() => usersTable.id),
  riderId: uuid("rider_id").notNull().references(() => usersTable.id),

  score: numeric("score", { precision: 5, scale: 2 }).notNull().default("0"),
  alongRoute: boolean("along_route").notNull().default(false),
  detourKm: numeric("detour_km", { precision: 6, scale: 2 }),

  status: bookingStatusEnum("status").notNull().default("PROPOSED"),

  driverEarning: numeric("driver_earning", { precision: 10, scale: 2 }),

  acceptedAt: timestamp("accepted_at", { withTimezone: true }),
  pickedUpAt: timestamp("picked_up_at", { withTimezone: true }),
  completedAt: timestamp("completed_at", { withTimezone: true }),

  createdAt: timestamp("created_at", { withTimezone: true }).notNull().defaultNow(),
  updatedAt: timestamp("updated_at", { withTimezone: true }).notNull().defaultNow().$onUpdate(() => new Date()),
});

export const insertBookingSchema = createInsertSchema(bookingsTable).omit({
  id: true,
  status: true,
  acceptedAt: true,
  pickedUpAt: true,
  completedAt: true,
  createdAt: true,
  updatedAt: true,
});

export type InsertBooking = z.infer<typeof insertBookingSchema>;
export type Booking = typeof bookingsTable.$inferSelect;
