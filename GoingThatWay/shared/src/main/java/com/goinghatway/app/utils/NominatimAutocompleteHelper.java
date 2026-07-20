package com.goinghatway.app.utils;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.AdapterView;

import com.goinghatway.app.api.NominatimService;
import com.goinghatway.app.models.NominatimPlace;

import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Attaches OSM Nominatim address autocomplete to an AutoCompleteTextView.
 *
 * Usage:
 * <pre>
 *   NominatimAutocompleteHelper.attach(
 *       autoCompleteView,
 *       (lat, lng, address) -> { originLat = lat; originLng = lng; }
 *   );
 * </pre>
 *
 * Design choices vs competitors:
 *  - Uber/Bolt both use Google Places autocomplete — same concept, but we use OSM (free, no key).
 *  - 400 ms debounce respects Nominatim's 1-req/sec policy and prevents mid-keystroke flicker.
 *  - Restricted to "za" (South Africa) so results are always locally relevant.
 */
public class NominatimAutocompleteHelper {

    private static final String BASE_URL      = "https://nominatim.openstreetmap.org/";
    private static final String COUNTRY_CODES = "za";
    private static final int    MAX_RESULTS   = 6;
    private static final long   DEBOUNCE_MS   = 450;
    private static final int    MIN_CHARS     = 3;

    /** Callback when the user taps a suggestion. */
    public interface OnPlaceSelected {
        void onSelected(double lat, double lng, String displayAddress);
    }

    private final AutoCompleteTextView view;
    private final OnPlaceSelected      listener;
    private final NominatimService     service;
    private final Handler              handler = new Handler(Looper.getMainLooper());

    // Prevent re-triggering search after the user selects an item
    private boolean selecting = false;

    private NominatimAutocompleteHelper(AutoCompleteTextView view, OnPlaceSelected listener) {
        this.view     = view;
        this.listener = listener;
        this.service  = buildService();
        attach();
    }

    /** Factory — attach autocomplete to the given view. */
    public static NominatimAutocompleteHelper attach(AutoCompleteTextView view,
                                                     OnPlaceSelected listener) {
        return new NominatimAutocompleteHelper(view, listener);
    }

    // ────────────────────────────────────────────────────────────────────────

    private NominatimService buildService() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    // Nominatim requires a descriptive User-Agent per usage policy
                    return chain.proceed(chain.request().newBuilder()
                            .header("User-Agent", "GoingThatWay Android App contact@goingthatway.app")
                            .header("Accept-Language", "en")
                            .build());
                })
                .build();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(NominatimService.class);
    }

    private void attach() {
        // When user taps a suggestion — store coords and update text
        view.setOnItemClickListener((parent, v, position, id) -> {
            NominatimPlace place = (NominatimPlace) parent.getItemAtPosition(position);
            if (place == null) return;
            selecting = true;
            view.setText(place.toString());
            view.dismissDropDown();
            listener.onSelected(place.getLat(), place.getLng(), place.displayName);
            selecting = false;
        });

        // Debounced text watcher
        view.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable e) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (selecting) return;
                handler.removeCallbacksAndMessages(null);
                if (s.length() >= MIN_CHARS) {
                    handler.postDelayed(() -> search(s.toString()), DEBOUNCE_MS);
                }
            }
        });
    }

    private void search(String query) {
        service.search(query, "json", COUNTRY_CODES, MAX_RESULTS)
                .enqueue(new Callback<List<NominatimPlace>>() {
                    @Override
                    public void onResponse(Call<List<NominatimPlace>> call,
                                           Response<List<NominatimPlace>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && !response.body().isEmpty()) {
                            List<NominatimPlace> places = response.body();
                            ArrayAdapter<NominatimPlace> adapter = new ArrayAdapter<>(
                                    view.getContext(),
                                    android.R.layout.simple_dropdown_item_1line,
                                    places);
                            handler.post(() -> {
                                view.setAdapter(adapter);
                                view.showDropDown();
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<List<NominatimPlace>> call, Throwable t) {
                        // Silent — user can still type a full address manually
                    }
                });
    }
}
