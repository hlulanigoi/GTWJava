package com.goinghatway.app.utils;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.text.TextUtils;


import java.io.IOException;
import java.util.List;
import java.util.Locale;

public final class LocationPickerHelper {

    private LocationPickerHelper() {}

    public static void autoFillFromAddress(Context context, String query, OnLocationResolved listener) {
        if (TextUtils.isEmpty(query)) {
            listener.onError("Please enter a location");
            return;
        }

        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocationName(query, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                listener.onResolved(address.getLatitude(), address.getLongitude(),
                        address.getAddressLine(0));
            } else {
                listener.onError("We could not find that location");
            }
        } catch (IOException e) {
            listener.onError("Location lookup failed");
        }
    }

    public static void reverseGeocode(Context context, double lat, double lng, OnAddressResolved listener) {
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                listener.onResolved(addresses.get(0).getAddressLine(0));
            } else {
                listener.onResolved("");
            }
        } catch (IOException e) {
            listener.onResolved("");
        }
    }

    public interface OnLocationResolved {
        void onResolved(double lat, double lng, String formattedAddress);
        void onError(String message);
    }

    public interface OnAddressResolved {
        void onResolved(String formattedAddress);
    }
}
