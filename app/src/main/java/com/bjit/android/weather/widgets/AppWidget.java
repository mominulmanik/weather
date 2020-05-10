package com.bjit.android.weather.widgets;

import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.bjit.android.weather.Constants;
import com.bjit.android.weather.R;
import com.bjit.android.weather.Utilis;
import com.bjit.android.weather.activities.SplashActivity;
import com.bjit.android.weather.model.City;
import com.bjit.android.weather.retrofit.responseModel.Currently;
import com.bjit.android.weather.services.RefreshJobService;
import com.google.gson.Gson;

import static android.content.Context.JOB_SCHEDULER_SERVICE;


public class AppWidget extends AppWidgetProvider {
    public static  Currently currently;


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction()!=null) {
            if (currently!=null){
                AppWidgetManager manager = AppWidgetManager.getInstance(context);
                int[] ids = manager.getAppWidgetIds(new ComponentName(context, AppWidget.class));
                onUpdate(context, manager, ids);

            }
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        Log.d("Widget","Updating widget");
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
        SharedPreferences sharedPref = Utilis.getSharedPref(context);
        String cityString = sharedPref.getString(Constants.CITY_EXTRA_DATA, "");

        Gson gson=new Gson();
        if (!cityString.equalsIgnoreCase("")){

            views.setTextViewText(R.id.tv_location, gson.fromJson(cityString, City.class).getAddress());
        }

        if (currently!=null){
            views.setTextViewText(R.id.tv_currentTemperature, Utilis.formatTemperature(context,currently.getTemperature()));
            views.setTextViewText(R.id.tv_weatherCondition, String.valueOf(currently.getSummary()));
            int imageForIcon = Utilis.getImageForIcon(currently.getIcon());
            if (imageForIcon!=-1)
                views.setImageViewResource(R.id.img_weatherIcon,imageForIcon);



        }
        Intent intent = new Intent(context, SplashActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent, 0);
        // Get the layout for the App Widget and attach an on-click listener to the button
        views.setOnClickPendingIntent(R.id.rl_widgetContainer, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);

    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {


    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        Log.d("Widget","Enabled");
        JobScheduler jobScheduler = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            jobScheduler = (JobScheduler)context.getSystemService(JOB_SCHEDULER_SERVICE);
        }

        ComponentName componentName = new ComponentName(context, RefreshJobService.class);
        long refresh_min = Long.parseLong(Utilis.getSharedPref(context).getString(Constants.PREF_KEY_REFRESH_RATE, String.valueOf(15)));

        JobInfo jobInfo = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobInfo.Builder builder = new JobInfo.Builder(RefreshJobService.JOBID, componentName)
                    .setPeriodic(refresh_min*60*1000) //this value will over ride with minimum 15 min in android >=N if it less than 15 min
                    .setPersisted(true)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);

            jobInfo = builder.build();

            if (jobScheduler != null) {
                int schedule = jobScheduler.schedule(jobInfo);
                if (schedule== JobScheduler.RESULT_SUCCESS){
                    Log.d("Widget","Job scheduled");
                }
            }else {
                Log.d("Widget","Job scheduler is null");
            }
        }
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled

        Log.d("Widget","Disabled");
        JobScheduler scheduler = (JobScheduler)
                context.getSystemService(JOB_SCHEDULER_SERVICE);

        if (scheduler != null) {
            for (JobInfo jobInfo : scheduler.getAllPendingJobs()) {
                if (jobInfo.getId() == RefreshJobService.JOBID) {
                    scheduler.cancel(RefreshJobService.JOBID);
                    Log.d("Widget","Cancelled Job with ID:" + RefreshJobService.JOBID);
                }
            }
        }
    }
}

