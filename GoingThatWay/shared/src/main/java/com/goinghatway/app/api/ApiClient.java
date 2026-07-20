package com.goinghatway.app.api;

import android.content.Context;

import com.goinghatway.app.utils.SessionManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class ApiClient {

    // Points at the backend running in this project's Replit workspace (artifacts/api-server).
    // Replace the host if you redeploy the backend elsewhere.
    private static final String BASE_URL = "https://20a2969b-48ee-4b7e-a88f-2701fe29aa80-00-q8zp81ep9p5q.picard.replit.dev/api/";

    // If you run the backend locally instead (pnpm --filter @workspace/api-server run dev):
    //   Android emulator:    http://10.0.2.2:8080/api/
    //   Physical device:     http://<your-computer-LAN-ip>:8080/api/
    // private static final String BASE_URL = "http://10.0.2.2:8080/api/";

    private static Retrofit retrofit;
    private static ApiService apiService;
    private static Context appContext;

    /** Call once from GoingThatWayApp.onCreate() so getInstance() works without a Context. */
    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }

    public static ApiService getService(Context context) {
        if (apiService == null) {
            apiService = getRetrofit(context).create(ApiService.class);
        }
        return apiService;
    }

    /** Raw Retrofit instance for building other Retrofit interfaces, e.g. AdminApiService. */
    public static Retrofit getInstance() {
        if (appContext == null) {
            throw new IllegalStateException(
                    "ApiClient.init(context) must be called before ApiClient.getInstance() — " +
                    "see GoingThatWayApp.onCreate()");
        }
        return getRetrofit(appContext);
    }

    private static Retrofit getRetrofit(Context context) {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            SessionManager sessionManager = new SessionManager(context);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        String token = sessionManager.getToken();
                        Request.Builder builder = original.newBuilder()
                                .header("Accept", "application/json")
                                .header("Content-Type", "application/json");
                        if (token != null && !token.isEmpty()) {
                            builder.header("Authorization", "Bearer " + token);
                        }
                        return chain.proceed(builder.build());
                    })
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    /** Call this when logging out to force recreation on next use */
    public static void reset() {
        retrofit = null;
        apiService = null;
    }
}
