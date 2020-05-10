package com.bjit.android.weather.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.bjit.android.weather.BuildConfig;
import com.bjit.android.weather.Constants;
import com.bjit.android.weather.R;
import com.bjit.android.weather.Utilis;
import com.bjit.android.weather.model.City;
import com.bjit.android.weather.widgets.AppWidget;
import com.bjit.android.weather.retrofit.APIClient;
import com.bjit.android.weather.retrofit.APIInterface;
import com.bjit.android.weather.retrofit.responseModel.EachHour;
import com.bjit.android.weather.retrofit.responseModel.Hourly;
import com.bjit.android.weather.retrofit.responseModel.TodaysWeatherResponse;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RefreshJobService extends JobService{

    Gson gson=new Gson();
    LatLng currentLatLng;
    City city;
    public static int JOBID=101;
    JobParameters params;
    APIInterface apiInterface;
    public RefreshJobService() {
    }

    @Override
    public boolean onStartJob(JobParameters params) {

        Log.d("JobService","Refresh Job Started");
        this.params=params;
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

        if (pm != null) {
            final PowerManager.WakeLock  wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getPackageName());
            //Acquire the lock
            wl.acquire(2*60*1000L);

            apiInterface= APIClient.getClient().create(APIInterface.class);
            String cityString = Utilis.getSharedPref(RefreshJobService.this).getString(Constants.CITY_EXTRA_DATA, "");

            if (!cityString.equalsIgnoreCase("")){

                city=gson.fromJson(cityString, City.class);
                currentLatLng=new LatLng(city.getLat(),city.getLng());
            }
            else return true;
            String addressFomLatLng = Utilis.getAddressFomLatLng(RefreshJobService.this, currentLatLng.latitude, currentLatLng.longitude);
            if (!Utilis.isNetworkConnected(this)){
                Toast.makeText(getApplicationContext(),"No internet available",Toast.LENGTH_SHORT).show();
                return true;
            }
            apiInterface.getTodaysWeather("forecast/"+ BuildConfig.dark_sky_apikey +"/"+currentLatLng.latitude+","+currentLatLng.longitude+"?units=si&exclude=flags").enqueue(new Callback<TodaysWeatherResponse>() {
                @Override
                public void onResponse(Call<TodaysWeatherResponse> call, Response<TodaysWeatherResponse> response) {
                    Log.d("retro","on response");

                    wl.release();
                    if (response.isSuccessful()){
                        AppWidget.currently= response.body().getCurrently();
                        Log.d("Job","broadcasting");
                        sendBroadcast(new Intent(RefreshJobService.this, AppWidget.class).setAction(Constants.ACTION_WIDGET_UPDATE));

                        prepareNotification(response.body().getHourly());

                    }
                    jobFinished(params,true);
                }

                @Override
                public void onFailure(Call<TodaysWeatherResponse> call, Throwable t) {
                    Log.d("retro","on fail "+t.getMessage());
                    wl.release();
                    jobFinished(params,true);

                }
            });


        }


        return true;
    }

    private void prepareNotification(Hourly hourly) {
        List<EachHour> data = hourly.getData();
        long timestamp = Utilis.getSharedPref(RefreshJobService.this).getLong(Constants.LOW_HIGH_NOTIFICATION_TIMESTAMP, 0);
        long temp = Utilis.getSharedPref(RefreshJobService.this).getInt(Constants.LOW_HIGH_TEMP, 0);
        String isNotificationEnable = Utilis.getSharedPref(RefreshJobService.this).getString(Constants.PREF_KEY_NOTIFICATION, "true");
        int isShow=0;
        EachHour showHour = null;
        for (int i=0;i<6;i++){
            EachHour eachHour = data.get(i);
            Float temperature = eachHour.getTemperature();
            int warnAbleTemp = Utilis.isWarnAbleTemp(RefreshJobService.this, temperature.intValue());

            if (isNotificationEnable.equalsIgnoreCase("true") && warnAbleTemp!=0 && (temp!=eachHour.getTemperature().intValue() || timestamp!=eachHour.getTime())){
                isShow=warnAbleTemp;
                showHour=eachHour;
            }
        }
        if(isShow!=0)
            showNotificationIfNecessary(isShow,showHour);


    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d("JobService","Job Stopped");

        return true;
    }



    private void showNotificationIfNecessary(int warnStatus, EachHour showHour) {
        Log.d("Refresh Job","showing notification= "+warnStatus+" with temp = "+showHour.getTemperature());

        SharedPreferences sharedPref = Utilis.getSharedPref(RefreshJobService.this);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(Constants.LOW_HIGH_NOTIFICATION_TIMESTAMP,showHour.getTime());
        editor.putInt(Constants.LOW_HIGH_TEMP,showHour.getTemperature().intValue());
        editor.apply();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "myChannel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.enableVibration(false);
            notificationChannel.setShowBadge(false);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        builder.setSmallIcon(R.mipmap.ic_launcher)

                .setShowWhen(true);
        if (warnStatus==-1) {
            builder.setContentText("Keep some warm cloths with you");
            builder.setContentTitle("Low temp");
        }else if(warnStatus==1){
            builder.setContentText("Wear comfortable cloths");
            builder.setContentTitle("High temp");
        }
        if (notificationManager != null) {
            notificationManager.notify(11033, builder.build());
        }
    }


}
