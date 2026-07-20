import type { User, Ride, Trip, Ticket, Booking, Payment, Parcel, Match } from "@workspace/db";

/** Convert Drizzle rows (camelCase, numeric-as-string) to the JSON shape the Android Gson models expect. */

export function toUserJson(u: User) {
  return {
    id: u.id,
    full_name: u.fullName,
    email: u.email,
    phone: u.phone,
    profile_picture: null,
    rating: Number(u.rating),
    total_rides_driven: u.totalRidesDriven,
    total_rides_taken: u.totalRidesTaken,
    tickets_owned: u.ticketsOwned,
    is_verified: true,
    role: u.role,
    is_active: u.isActive,
    is_approved_driver: u.isApprovedDriver,
    license_number: u.licenseNumber,
    vehicle_plate: u.vehiclePlate,
    vehicle_model: u.vehicleModel,
    created_at: u.createdAt,
  };
}

export function toRideJson(
  r: Ride,
  opts: { rider?: User | null; driver?: User | null } = {},
) {
  return {
    id: r.id,
    rider_id: r.riderId,
    rider: opts.rider ? toUserJson(opts.rider) : null,
    driver_id: r.driverId,
    driver: opts.driver ? toUserJson(opts.driver) : null,
    ride_type: r.rideType,
    notes: r.notes,
    passenger_count: r.passengerCount,
    luggage_size: r.luggageSize,
    pickup_address: r.pickupAddress,
    pickup_lat: Number(r.pickupLat),
    pickup_lng: Number(r.pickupLng),
    destination_address: r.destinationAddress,
    destination_lat: Number(r.destinationLat),
    destination_lng: Number(r.destinationLng),
    fare: Number(r.fare),
    driver_earning: Number(r.driverEarning),
    platform_fee: Number(r.platformFee),
    payment_reference: r.paymentReference,
    payment_status: r.paymentVerified === "true" ? "CONFIRMED" : "PENDING",
    status: r.status,
    created_at: r.createdAt,
  };
}

export function toTripJson(
  t: Trip,
  opts: {
    driver?: User | null;
    matchedRides?: Ride[];
    matchedRideCount?: number;
  } = {},
) {
  return {
    id: t.id,
    driver_id: t.driverId,
    driver: opts.driver ? toUserJson(opts.driver) : null,
    origin_address: t.originAddress,
    origin_lat: Number(t.originLat),
    origin_lng: Number(t.originLng),
    destination_address: t.destinationAddress,
    destination_lat: Number(t.destinationLat),
    destination_lng: Number(t.destinationLng),
    departure_time: t.departureTime,
    arrival_time: t.arrivalTime,
    transport_mode: t.transportMode,
    seats_available: t.seatsAvailable,
    waypoints: [],
    matched_rides: (opts.matchedRides ?? []).map((r) => toRideJson(r)),
    matched_ride_count: opts.matchedRideCount ?? 0,
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

export function toBookingJson(
  b: Booking,
  opts: { ride?: Ride | null; trip?: Trip | null } = {},
) {
  return {
    id: b.id,
    ride_id: b.rideId,
    ride: opts.ride ? toRideJson(opts.ride) : null,
    trip_id: b.tripId,
    trip: opts.trip ? toTripJson(opts.trip) : null,
    match_score: Number(b.score),
    detour_distance_km: b.detourKm ? Number(b.detourKm) : 0,
    is_along_route: b.alongRoute,
    status: b.status,
    driver_earning: b.driverEarning ? Number(b.driverEarning) : null,
    proposed_at: b.createdAt,
    accepted_at: b.acceptedAt,
    picked_up_at: b.pickedUpAt,
    completed_at: b.completedAt,
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

export function toParcelJson(p: Parcel) {
  return {
    id: p.id,
    sender_id: p.senderId,
    carrier_id: p.carrierId,
    description: p.description,
    weight: Number(p.weight),
    size: p.size,
    pickup_address: p.pickupAddress,
    pickup_lat: Number(p.pickupLat),
    pickup_lng: Number(p.pickupLng),
    destination_address: p.destinationAddress,
    destination_lat: Number(p.destinationLat),
    destination_lng: Number(p.destinationLng),
    status: p.status,
    fee: Number(p.fee),
    platform_fee: Number(p.platformFee),
    carrier_earning: Number(p.carrierEarning),
    payment_reference: p.paymentReference,
    payment_verified: p.paymentVerified === "true",
    special_instructions: p.specialInstructions,
    created_at: p.createdAt,
    updated_at: p.updatedAt,
  };
}

export function toMatchJson(
  m: Match,
  opts: { parcel?: Parcel | null; trip?: Trip | null } = {},
) {
  return {
    id: m.id,
    trip_id: m.tripId,
    trip: opts.trip ? toTripJson(opts.trip) : null,
    parcel_id: m.parcelId,
    parcel: opts.parcel ? toParcelJson(opts.parcel) : null,
    carrier_id: m.carrierId,
    sender_id: m.senderId,
    score: Number(m.score),
    along_route: m.alongRoute,
    detour_km: m.detourKm ? Number(m.detourKm) : 0,
    carrier_earning: m.carrierEarning ? Number(m.carrierEarning) : null,
    status: m.status,
    accepted_at: m.acceptedAt,
    collected_at: m.collectedAt,
    delivered_at: m.deliveredAt,
    created_at: m.createdAt,
  };
}
