package com.bjit.android.weather;

import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import com.bjit.android.weather.app.App;
import com.bjit.android.weather.services.NotificationJobService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static android.content.Context.JOB_SCHEDULER_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class Utilis {



    public static String getAddressFomLatLng(Context context,double latitude,double longitude){
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(context, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            if(addresses.size()>0){
                Log.e("UtilAdd","geocoder result "+addresses);


                return addresses.get(0).getLocality();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unavailable";
    }


    public static String secondsToComponentTimes(long seconds)
    {

        int hours = (int) seconds / 3600;
        int remainder = (int) seconds - hours * 3600;
        int mins = remainder / 60;
        remainder = remainder - mins * 60;
        int secs = remainder;

        return String.format(Locale.getDefault(),"%02d:%02d:%02d", hours, mins, secs);
    }
    public static float convertDpToPx(Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    static void showExitDialog(Activity activity){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        activity.finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }
    public static String formatTemperature(Context context,double temp){
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(context);

        String temp_unit = sharedPreferences.getString(Constants.PREF_KEY_TEMP_UNIT, "\u2103");


        if (temp_unit.equals("\u2103")){
            return String.format(Locale.getDefault(),"%.02f", temp) +temp_unit;
        }else
            return  String.format(Locale.getDefault(),"%.02f", (9.0/5.0)*temp + 32)+temp_unit;

    }
    public static SharedPreferences getSharedPref(Context context){

        return context.getSharedPreferences("weatherPref", MODE_PRIVATE);

    }

    public static String getFormattedDate(long timestampInSec, String pattern){

        //Log.e("DateFormat", ""+timestampInSec);
        try {

            DateFormat dateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestampInSec*1000);
            return dateFormat.format(calendar.getTime());
        }catch (Exception e){
            Log.e("DateFormat", Objects.requireNonNull(e.getMessage()));
            return "Unavailable";
        }

    }
    public static int getImageForIcon(String iconName){
        Log.d("Utilis","Image icon name= "+iconName);

        switch (iconName){
            case "clear-day":

                return R.drawable.ic_clear_day;
            case "clear-night":
                return R.drawable.ic_clear_night;
            case "cloudy":
                return R.drawable.ic_cloudy;
            case "fog":
                return R.drawable.ic_fog;
            case "hali":
                return R.drawable.ic_hail;
            case "partly-cloudy-day":
                return R.drawable.ic_partly_cloudy_day;
            case "partly-cloudy-night":
                return R.drawable.ic_partly_cloudy_night;
            case "rain":
                return R.drawable.ic_rain;
            case "sleet":
                return R.drawable.ic_sleet;
            case "snow":
                return R.drawable.ic_snow;
            case "thunderstorm":
                return R.drawable.ic_thunderstorm;
            case "tornado":
                return R.drawable.ic_tornado;
            default:
                    return -1;

        }

    }
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
    public static int isWarnAbleTemp(Context context,int temp){

        String min=getSharedPref(context).getString(Constants.PREF_KEY_MIN_TEMP, "15");
        String max=getSharedPref(context).getString(Constants.PREF_KEY_MAX_TEMP,"32");
        int minint=Integer.parseInt(min);
        int maxint=Integer.parseInt(max);
        if (temp<=minint)
            return  -1;
        else if (temp>=maxint)
            return 1;
        else
            return 0;


    }

    public static String getWeatherCondition(int aqius){

        String pred="";
        if (isBetween(aqius,0,50))
            pred= "Good";
        else if (isBetween(aqius,51,100))
            pred= "Moderate";

        else if(isBetween(aqius,101,150))
            pred= "Unhealthy for sensitive group";
        else if (isBetween(aqius,151,200))
            pred= "Unhealthy";
        else if(isBetween(aqius,201,300))
            pred= "Very unhealthy";
        else
            pred= "Hazardous";


        return  pred;
    }
    private static boolean isBetween(int x, int lower, int upper) {
        return lower <= x && x <= upper;
    }

    public static void notificationJobSchedule() {
        JobScheduler jobScheduler = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            jobScheduler = (JobScheduler) App.context.getSystemService(JOB_SCHEDULER_SERVICE);
        }

        ComponentName componentName = new ComponentName(App.context, NotificationJobService.class);
        JobInfo jobInfo = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobInfo.Builder builder = new JobInfo.Builder(NotificationJobService.JOBID, componentName)
                    .setPeriodic(Constants.NOTIFICATION_TIME_INTERVAL) //this value will over ride with minimum 15 min in android >=N if it is less than 15 min
                    .setPersisted(true)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setRequiresBatteryNotLow(true);
            }

            jobInfo = builder.build();

            if (jobScheduler != null) {
                int schedule = jobScheduler.schedule(jobInfo);
                if (schedule== JobScheduler.RESULT_SUCCESS){
                    Log.d("NotificationJob","Job scheduled");
                }
            }else {
                Log.d("NotificationJob","Job scheduler is null");
            }
        }
    }


    public static void NotificationjobCancel() {
        JobScheduler scheduler = (JobScheduler)
                App.context.getSystemService(JOB_SCHEDULER_SERVICE);

        if (scheduler != null) {
            for (JobInfo jobInfo : scheduler.getAllPendingJobs()) {
                if (jobInfo.getId() == NotificationJobService.JOBID) {
                    scheduler.cancel(NotificationJobService.JOBID);
                    Log.d("Widget","Cancelled Job with ID:" + NotificationJobService.JOBID);
                }
            }
        }
    }

}
