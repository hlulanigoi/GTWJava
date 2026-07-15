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

export const ticketStatusEnum = pgEnum("ticket_status", [
  "PENDING",
  "ACTIVE",
  "USED",
  "EXPIRED",
]);

export const ticketsTable = pgTable("tickets", {
  id: uuid("id").primaryKey().defaultRandom(),
  userId: uuid("user_id").notNull().references(() => usersTable.id),

  code: text("code").notNull().unique(),
  pricePaid: numeric("price_paid", { precision: 8, scale: 2 }).notNull(),
  paymentReference: text("payment_reference").notNull(),

  status: ticketStatusEnum("status").notNull().default("PENDING"),

  purchasedAt: timestamp("purchased_at", { withTimezone: true }).notNull().defaultNow(),
  expiresAt: timestamp("expires_at", { withTimezone: true }),
  usedAt: timestamp("used_at", { withTimezone: true }),

  createdAt: timestamp("created_at", { withTimezone: true }).notNull().defaultNow(),
  updatedAt: timestamp("updated_at", { withTimezone: true }).notNull().defaultNow().$onUpdate(() => new Date()),
});

export const insertTicketSchema = createInsertSchema(ticketsTable).omit({
  id: true,
  status: true,
  purchasedAt: true,
  usedAt: true,
  createdAt: true,
  updatedAt: true,
});

export type InsertTicket = z.infer<typeof insertTicketSchema>;
export type Ticket = typeof ticketsTable.$inferSelect;
