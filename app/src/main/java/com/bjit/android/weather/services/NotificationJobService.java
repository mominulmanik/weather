package com.bjit.android.weather.services;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.bjit.android.weather.Constants;
import com.bjit.android.weather.R;
import com.bjit.android.weather.Utilis;
import com.bjit.android.weather.retrofit.APIClient;
import com.bjit.android.weather.retrofit.APIInterface;
import com.bjit.android.weather.retrofit.responseModel.pollution.PollutionResponse;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class NotificationJobService extends JobService{

    public static int JOBID=102;
    JobParameters params;
    APIInterface apiInterface;
    public NotificationJobService() {
    }

    @Override
    public boolean onStartJob(JobParameters params) {

        Log.d("NotificationJobService","Job Started");


        this.params=params;
        apiInterface= APIClient.getClient().create(APIInterface.class);
        checkEnableLocation();
        return true;
    }
    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d("NotificationJobService","Job Stopped");

        return true;
    }

    private void showNotificationIfNecessary(String city, int aqius) {

        SharedPreferences sharedPref = Utilis.getSharedPref(NotificationJobService.this);
        String notifiedCity = sharedPref.getString(Constants.LAST_NOTIFIED_CITY, "");
        long notifiedTime = sharedPref.getLong(Constants.LAST_NOTIFIED_TIME, 0);
        String notifiedCondition = sharedPref.getString(Constants.LAST_NOTIFIED_CONDITION, "");
        String weatherCondition = Utilis.getWeatherCondition(aqius);
        long currentTimeMillis = System.currentTimeMillis();

        if (!city.equalsIgnoreCase(notifiedCity) || !weatherCondition.equalsIgnoreCase(notifiedCondition) || Math.abs(currentTimeMillis-notifiedTime)>=Constants.NOTIFICATION_TIME_INTERVAL) {

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(Constants.LAST_NOTIFIED_CITY,city);
            editor.putString(Constants.LAST_NOTIFIED_CONDITION,weatherCondition);
            editor.putLong(Constants.LAST_NOTIFIED_TIME,currentTimeMillis);
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
                    .setContentTitle("Weather alert")
                    .setContentText(weatherCondition)
                    .setContentInfo(city)
                    .setShowWhen(true);
            if (notificationManager != null) {
                notificationManager.notify(11032, builder.build());
            }
        }
    }

    private void checkEnableLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            jobFinished(params,true);
            return;

        }

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setNumUpdates(1);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        Task<LocationSettingsResponse> task = LocationServices.getSettingsClient(NotificationJobService.this).checkLocationSettings(builder.build());
        task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.d("NotificationJobService","getting location");
                fetchLocation();
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                jobFinished(params,true);
            }
        });
    }


    private void fetchLocation(){

        if (!Utilis.isNetworkConnected(this)){
            jobFinished(params,true);
            return ;
        }

        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                if (location != null) {

                    Log.d("NotificationJobService","getting data");
                    apiInterface.getPollutedCityNearMe("https://api.airvisual.com/v2/nearest_city?lat="+location.getLatitude()+"&lon="+location.getLongitude()+"&key=22651858-b350-4a9a-8f34-e07af9ce9474").enqueue(new Callback<PollutionResponse>() {
                        @Override
                        public void onResponse(Call<PollutionResponse> call, Response<PollutionResponse> response) {
                            Log.d("retro","on response");


                            if (response.isSuccessful() && response.body().getStatus().equalsIgnoreCase("Success")){
                                String city = response.body().getData().getCity();
                                int aqius = response.body().getData().getCurrent().getPollution().getAqius();
                                showNotificationIfNecessary(city,aqius);


                            }
                            jobFinished(params,true);
                        }

                        @Override
                        public void onFailure(Call<PollutionResponse> call, Throwable t) {
                            Log.d("retro","on fail "+t.getMessage());
                            jobFinished(params,true);

                        }
                    });



                }
                jobFinished(params,true);
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("NotificationJob","Failed to get location");
                jobFinished(params,true);

            }
        });
    }


}
