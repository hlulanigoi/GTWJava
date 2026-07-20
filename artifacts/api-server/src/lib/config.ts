import { Constants } from "./constants";

/**
 * Mutable runtime configuration.
 * Values persist for the lifetime of the process; they reset on restart.
 * Used for settings the admin can change without a deploy (e.g. ticket price).
 */
export const runtimeConfig = {
  ticketPrice: Constants.DEFAULT_TICKET_PRICE,
};
