package com.goinghatway.app.models;

import com.google.gson.annotations.SerializedName;

/**
 * One result from the OpenStreetMap Nominatim search API.
 * No API key required — abide by the usage policy (include User-Agent header).
 */
public class NominatimPlace {

    @SerializedName("place_id")    public long   placeId;
    @SerializedName("display_name") public String displayName;
    @SerializedName("lat")          public String lat;
    @SerializedName("lon")          public String lon;
    @SerializedName("type")         public String type;

    public double getLat() { return Double.parseDouble(lat != null ? lat : "0"); }
    public double getLng() { return Double.parseDouble(lon != null ? lon : "0"); }

    /**
     * Short label shown in the AutoCompleteTextView dropdown.
     * Nominatim returns very long strings like "Sandton, City of Johannesburg, Gauteng, South Africa"
     * — we keep the first three segments max.
     */
    @Override
    public String toString() {
        if (displayName == null) return "";
        String[] parts = displayName.split(", ");
        if (parts.length >= 3) {
            return parts[0] + ", " + parts[1] + ", " + parts[2];
        }
        return displayName.length() > 70 ? displayName.substring(0, 70) + "…" : displayName;
    }
}
