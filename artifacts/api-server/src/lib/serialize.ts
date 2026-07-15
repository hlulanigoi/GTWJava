import type { User, Parcel, Trip, Ticket, Match, Payment } from "@workspace/db";

/** Convert Drizzle rows (camelCase, numeric-as-string) to the JSON shape the Android Gson models expect. */

export function toUserJson(u: User) {
  return {
    id: u.id,
    full_name: u.fullName,
    email: u.email,
    phone: u.phone,
    profile_picture: null,
    rating: Number(u.rating),
    total_deliveries: u.totalDeliveries,
    total_parcels_sent: u.totalParcelsSent,
    tickets_owned: u.ticketsOwned,
    is_verified: true,
    role: u.role,
    is_active: u.isActive,
    created_at: u.createdAt,
  };
}

export function toParcelJson(
  p: Parcel,
  opts: { sender?: User | null; carrier?: User | null } = {},
) {
  return {
    id: p.id,
    sender_id: p.senderId,
    sender: opts.sender ? toUserJson(opts.sender) : null,
    carrier_id: p.carrierId,
    carrier: opts.carrier ? toUserJson(opts.carrier) : null,
    description: p.description,
    weight_kg: Number(p.weight),
    size_label: p.size,
    pickup_address: p.pickupAddress,
    pickup_lat: Number(p.pickupLat),
    pickup_lng: Number(p.pickupLng),
    destination_address: p.destinationAddress,
    destination_lat: Number(p.destinationLat),
    destination_lng: Number(p.destinationLng),
    fee: Number(p.fee),
    carrier_earnings: Number(p.carrierEarning),
    platform_fee: Number(p.platformFee),
    payment_reference: p.paymentReference,
    payment_status: p.paymentVerified === "true" ? "CONFIRMED" : "PENDING",
    status: p.status,
    image_url: null,
    special_instructions: p.specialInstructions,
    created_at: p.createdAt,
    collected_at: null,
    delivered_at: null,
  };
}

export function toTripJson(
  t: Trip,
  opts: {
    traveler?: User | null;
    matchedParcels?: Parcel[];
    matchedParcelCount?: number;
  } = {},
) {
  return {
    id: t.id,
    traveler_id: t.carrierId,
    traveler: opts.traveler ? toUserJson(opts.traveler) : null,
    origin_address: t.originAddress,
    origin_lat: Number(t.originLat),
    origin_lng: Number(t.originLng),
    destination_address: t.destinationAddress,
    destination_lat: Number(t.destinationLat),
    destination_lng: Number(t.destinationLng),
    departure_time: t.departureTime,
    arrival_time: t.arrivalTime,
    transport_mode: t.transportMode,
    available_capacity_kg: Number(t.capacityKg),
    waypoints: [],
    matched_parcels: (opts.matchedParcels ?? []).map((p) => toParcelJson(p)),
    matched_parcel_count: opts.matchedParcelCount ?? 0,
    status: t.status,
    notes: t.notes,
    created_at: t.createdAt,
  };
}

export function toTicketJson(t: Ticket, owner?: User | null) {
  return {
    id: t.id,
    owner_id: t.userId,
    owner: owner ? toUserJson(owner) : null,
    ticket_code: t.code,
    price_paid: Number(t.pricePaid),
    status: t.status,
    used_for_trip_id: null,
    payment_reference: t.paymentReference,
    purchased_at: t.purchasedAt,
    expires_at: t.expiresAt,
    used_at: t.usedAt,
  };
}

export function toMatchJson(
  m: Match,
  opts: { parcel?: Parcel | null; trip?: Trip | null } = {},
) {
  return {
    id: m.id,
    parcel_id: m.parcelId,
    parcel: opts.parcel ? toParcelJson(opts.parcel) : null,
    trip_id: m.tripId,
    trip: opts.trip ? toTripJson(opts.trip) : null,
    match_score: Number(m.score),
    detour_distance_km: m.detourKm ? Number(m.detourKm) : 0,
    is_along_route: m.alongRoute,
    status: m.status,
    proposed_at: m.createdAt,
    responded_at: m.acceptedAt,
  };
}

export function toPaymentJson(p: Payment) {
  return {
    id: p.id,
    user_id: p.userId,
    reference: p.reference,
    amount: Number(p.amount),
    type: p.type,
    status: p.status,
    rejection_reason: p.rejectionReason,
    created_at: p.createdAt,
    verified_at: p.verifiedAt,
  };
}
