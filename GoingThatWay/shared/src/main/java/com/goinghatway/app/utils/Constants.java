package com.goinghatway.app.utils;

public class Constants {

    // SharedPreferences keys
    public static final String PREF_NAME        = "going_that_way_prefs";
    public static final String KEY_TOKEN        = "auth_token";
    public static final String KEY_USER_ID      = "user_id";
    public static final String KEY_USER_NAME    = "user_name";
    public static final String KEY_USER_EMAIL   = "user_email";
    public static final String KEY_USER_ROLE    = "user_role";
    public static final String KEY_IS_LOGGED_IN = "is_logged_in";

    // Intent extras
    public static final String EXTRA_RIDE_ID    = "extra_ride_id";
    public static final String EXTRA_TRIP_ID    = "extra_trip_id";
    public static final String EXTRA_BOOKING_ID = "extra_booking_id";
    public static final String EXTRA_TICKET_ID  = "extra_ticket_id";
    public static final String EXTRA_RIDE       = "extra_ride";
    public static final String EXTRA_TRIP       = "extra_trip";
    public static final String EXTRA_AMOUNT     = "extra_amount";
    public static final String EXTRA_PURPOSE    = "extra_purpose";
    public static final String EXTRA_USER_ID    = "extra_user_id";
    public static final String EXTRA_PARCEL_ID  = "extra_parcel_id";

    // Platform fee
    public static final double PLATFORM_FEE_PERCENT  = 0.20;
    public static final double CARRIER_SHARE_PERCENT = 0.80;

    // Ticket price (overridden from server)
    public static final double DEFAULT_TICKET_PRICE = 50.0;

    // Route matching
    public static final double MAX_DETOUR_KM   = 5.0;
    public static final double ROUTE_BUFFER_KM = 2.0;

    // Luggage sizes
    public static final String SIZE_NONE   = "NONE";
    public static final String SIZE_SMALL  = "SMALL";
    public static final String SIZE_MEDIUM = "MEDIUM";
    public static final String SIZE_LARGE  = "LARGE";

    // Transport modes
    public static final String MODE_CAR   = "CAR";
    public static final String MODE_BUS   = "BUS";
    public static final String MODE_TRAIN = "TRAIN";
    public static final String MODE_WALK  = "WALK";
    public static final String MODE_OTHER = "OTHER";

    // Request codes
    public static final int RC_LOCATION_PERMISSION = 1001;
    public static final int RC_CAMERA_PERMISSION   = 1002;
    public static final int RC_PICK_IMAGE          = 1003;
    public static final int RC_CREATE_RIDE         = 2001;
    public static final int RC_POST_TRIP           = 2002;
    public static final int RC_BUY_TICKET          = 2003;
    public static final int RC_PAYMENT             = 2004;
    public static final int RC_CREATE_PARCEL       = 2005;
}
