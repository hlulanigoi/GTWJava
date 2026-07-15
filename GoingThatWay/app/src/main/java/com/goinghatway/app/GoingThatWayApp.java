package com.goinghatway.app;

import android.app.Application;

import com.goinghatway.app.api.ApiClient;

public class GoingThatWayApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ApiClient.init(this);
    }
}
