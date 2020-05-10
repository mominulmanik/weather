package com.bjit.android.weather.app;

import android.app.Application;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.Context;
import android.util.Log;

import com.bjit.android.weather.services.RefreshJobService;

public class App extends Application {
    public static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context=getApplicationContext();

    }

}
