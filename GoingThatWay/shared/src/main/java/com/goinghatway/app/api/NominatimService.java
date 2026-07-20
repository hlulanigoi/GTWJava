package com.goinghatway.app.api;

import com.goinghatway.app.models.NominatimPlace;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Retrofit interface for the public OpenStreetMap Nominatim geocoding API.
 * Base URL: https://nominatim.openstreetmap.org/
 * No API key required. Must include a descriptive User-Agent header (set in client builder).
 * Rate limit: max 1 request per second — enforced by the debounce in NominatimAutocompleteHelper.
 */
public interface NominatimService {

    /**
     * Free-form address search.
     *
     * @param query        What the user typed
     * @param format       Always "json"
     * @param countryCodes Comma-separated ISO-3166 codes, e.g. "za" to bias South Africa
     * @param limit        Max results (5 is enough for a dropdown)
     */
    @GET("search")
    Call<List<NominatimPlace>> search(
            @Query("q")            String query,
            @Query("format")       String format,
            @Query("countrycodes") String countryCodes,
            @Query("limit")        int    limit
    );
}
