package com.bjit.android.weather.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bjit.android.weather.BuildConfig;
import com.bjit.android.weather.Constants;
import com.bjit.android.weather.R;
import com.bjit.android.weather.Utilis;
import com.bjit.android.weather.adapters.DailyForecastRVAdapter;
import com.bjit.android.weather.adapters.HourlyForecastRVAdapter;
import com.bjit.android.weather.interfaces.RVClickListener;
import com.bjit.android.weather.model.City;
import com.bjit.android.weather.model.CustomGraphDataSet;
import com.bjit.android.weather.widgets.AppWidget;
import com.bjit.android.weather.retrofit.APIClient;
import com.bjit.android.weather.retrofit.APIInterface;
import com.bjit.android.weather.retrofit.responseModel.Currently;
import com.bjit.android.weather.retrofit.responseModel.EachDay;
import com.bjit.android.weather.retrofit.responseModel.TodaysWeatherResponse;
import com.bjit.android.weather.services.RefreshJobService;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements RVClickListener {

    private static final int PERMISSIONS_REQUEST_LOCATION = 102;
    private static final int PERMISSION_REQUEST_CHECK_SETTINGS = 103;
    private boolean isLocationEnabled=false;
    @BindView(R.id.imgv_weatherIcon)
    AppCompatImageView imgv_weatherIcon;
    @BindView(R.id.tv_headerTitle)
    AppCompatTextView tv_headerTitle;
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.tv_currentTemperature)
    AppCompatTextView tv_currentTemp;
    @BindView(R.id.tv_weatherCondition)
    AppCompatTextView tv_weatherCondition;
    @BindView(R.id.tv_lastUpdate)
    AppCompatTextView tv_lastUpdate;
    @BindView(R.id.rv_dailyForecast)
    RecyclerView rv_dailyForecast;
    @BindView(R.id.rv_hourlyForecast)
    RecyclerView rv_hourlyForecast;
    @BindView(R.id.rl_dashBoardContainer)
    RelativeLayout rl_dashBoardContainer;

    List<EachDay> lists;

    DailyForecastRVAdapter dailyForecastRVAdapter;
    HourlyForecastRVAdapter hourlyForecastRVAdapter;
    JobScheduler jobScheduler;
    LocationRequest mLocationRequest;

    APIInterface apiInterface;
    LatLng currentLatLng;
    Gson gson;
    City city;
    @BindView(R.id.chart)
    LineChart chart;
    CustomGraphDataSet customGraphDataSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        rl_dashBoardContainer.setVisibility(View.GONE);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.refresh_progress_1,
                R.color.refresh_progress_2,
                R.color.refresh_progress_3,
                R.color.refresh_progress_4);
        swipeRefreshLayout.setOnRefreshListener(this::updateWeatherData);

        apiInterface=APIClient.getClient().create(APIInterface.class);
        lists= new ArrayList<>();
        dailyForecastRVAdapter=new DailyForecastRVAdapter(getApplicationContext(),this );
        hourlyForecastRVAdapter=new HourlyForecastRVAdapter(getApplicationContext(),null);


        gson=new Gson();
        rv_dailyForecast.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        rv_hourlyForecast.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));


        rv_hourlyForecast.setAdapter(hourlyForecastRVAdapter);
        rv_dailyForecast.setAdapter(dailyForecastRVAdapter);

        DividerItemDecoration dividerItemDecorationHourly = new DividerItemDecoration(rv_hourlyForecast.getContext(),
                LinearLayoutManager.HORIZONTAL);
        DividerItemDecoration dividerItemDecorationDaily = new DividerItemDecoration(rv_dailyForecast.getContext(),
                LinearLayoutManager.HORIZONTAL);
        dividerItemDecorationHourly.setDrawable(getResources().getDrawable(R.drawable.divider));
        dividerItemDecorationDaily.setDrawable(getResources().getDrawable(R.drawable.divider));
        rv_hourlyForecast.addItemDecoration(dividerItemDecorationHourly);
        rv_dailyForecast.addItemDecoration(dividerItemDecorationDaily);

        SharedPreferences sharedPref = Utilis.getSharedPref(this);
        String cityString = sharedPref.getString(Constants.CITY_EXTRA_DATA, "");

        if (!cityString.equalsIgnoreCase("")){

            city=gson.fromJson(cityString,City.class);
            tv_headerTitle.setText(city.getAddress());
        }

        Intent intent = getIntent();
        if (intent!=null) {
            final Bundle extras = intent.getExtras();

            if (extras != null) {
                String jsonExtra = extras.getString(Constants.CITY_EXTRA_DATA, "");
                if (!jsonExtra.equalsIgnoreCase("")){

                    city=gson.fromJson(jsonExtra,City.class);
                    tv_headerTitle.setText(city.getAddress());
                }
            }
        }

        if(city==null) {
            buildLocationRequest();
        }else{
            currentLatLng=new LatLng(city.getLat(),city.getLng());
            updateWeatherData();
        }
        jobSchedule();
        String aTrue = Utilis.getSharedPref(this).getString(Constants.PREF_KEY_NOTIFICATION, "true");
        if (aTrue.equalsIgnoreCase("true"))
            Utilis.notificationJobSchedule();
        else
            Utilis.NotificationjobCancel();


    }


    private void updateWeatherData() {
        if (currentLatLng==null){
            buildLocationRequest();
            return;
        }

        if (!Utilis.isNetworkConnected(this)){
            Toast.makeText(getApplicationContext(),"No internet available",Toast.LENGTH_SHORT).show();
            return;
        }

        String addressFomLatLng = Utilis.getAddressFomLatLng(MainActivity.this, currentLatLng.latitude, currentLatLng.longitude);
        Log.d("addressFromLatlng","= "+addressFomLatLng);

        swipeRefreshLayout.setRefreshing(true);
        apiInterface.getTodaysWeather("forecast/"+ BuildConfig.dark_sky_apikey +"/"+currentLatLng.latitude+","+currentLatLng.longitude+"?units=si&exclude=flags").enqueue(new Callback<TodaysWeatherResponse>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(Call<TodaysWeatherResponse> call, Response<TodaysWeatherResponse> response) {
                Log.d("retro","on response");

                if (response.isSuccessful()){

                    try {
                        Currently currently = response.body().getCurrently();
                        AppWidget.currently = currently;
                        sendBroadcast(new Intent(MainActivity.this, AppWidget.class).setAction(Constants.ACTION_WIDGET_UPDATE));


                        tv_currentTemp.setText(Utilis.formatTemperature(MainActivity.this, currently.getTemperature()));
                        tv_weatherCondition.setText(currently.getSummary());
                        tv_lastUpdate.setText("Last update: " + Utilis.getFormattedDate(currently.getTime(), "dd/MM HH:mm"));
                        int imageForIcon = Utilis.getImageForIcon(currently.getIcon());
                        if (imageForIcon != -1)
                            imgv_weatherIcon.setImageResource(imageForIcon);

                        if (city == null || city.getAddress().equals("") || city.getAddress() == null) {
                            if (addressFomLatLng==null)
                                tv_headerTitle.setText("Unknown Address");
                            else
                                tv_headerTitle.setText(addressFomLatLng);
                            City city1 = new City();
                            city1.setAddress(addressFomLatLng);
                            city1.setLat(currentLatLng.latitude);
                            city1.setLng(currentLatLng.longitude);
                            String toJson = gson.toJson(city1);
                            SharedPreferences.Editor editor = Utilis.getSharedPref(MainActivity.this).edit();
                            editor.putString(Constants.CITY_EXTRA_DATA,toJson);
                            editor.apply();
                        }
                        else
                            tv_headerTitle.setText(city.getAddress());


                        dailyForecastRVAdapter.setLists(response.body().getDaily().getData());
                        dailyForecastRVAdapter.notifyDataSetChanged();
                        hourlyForecastRVAdapter.setLists(response.body().getHourly().getData());
                        hourlyForecastRVAdapter.notifyDataSetChanged();
                        customGraphDataSet = response.body().getDaily().getPreparedGraphData();
                        rl_dashBoardContainer.setVisibility(View.VISIBLE);


                        drawGraph();
                    }catch (Exception e){
                        Log.d("MainAct", Objects.requireNonNull(e.getMessage()));
                    }


                }else {
                    Toast.makeText(MainActivity.this,"Failed to get weather update",Toast.LENGTH_SHORT).show();
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<TodaysWeatherResponse> call, Throwable t) {
                Log.d("retro","on fail "+t.getMessage());
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(MainActivity.this,"Failed to get weather update",Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void drawGraph() {
        LineDataSet set = new LineDataSet(customGraphDataSet.getAllMaxDataAsEntry(),"Max Temperature");
        set.setColor(Color.YELLOW);
        set.setCircleColor(Color.YELLOW);
        set.setLineWidth(2.0f);
        set.setCircleRadius(3.0f);
        set.setDrawFilled(true);
        set.notifyDataSetChanged();





        LineDataSet set2 = new LineDataSet(customGraphDataSet.getAllMinDataAsEntry(),"Min Temperature");
        set2.setColor(Color.WHITE);
        set2.setCircleColor(Color.WHITE);
        set2.setLineWidth(2.0f);
        set2.setCircleRadius(3.0f);
        set2.setDrawFilled(true);
        set2.notifyDataSetChanged();

        LineData data = new LineData();
        data.addDataSet(set);
        data.addDataSet(set2);
        data.setHighlightEnabled(true);
        data.notifyDataChanged();


        chart.setData(data);
        chart.setDrawGridBackground(false);
        chart.getDescription().setEnabled(false);
        chart.setExtraOffsets(10,0,10,10);

        chart.notifyDataSetChanged();
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setEnabled(false);
        XAxis xAxis = chart.getXAxis();

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                super.getAxisLabel(value, axis);

                if (((int) value) >= customGraphDataSet.getModelList().size() || value<0) {
                    return "";
                }

                return customGraphDataSet.getLabelForIndex((int)value);
            }
        });
        chart.setPinchZoom(false);
        chart.invalidate();
    }

    private void jobSchedule() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            jobScheduler = (JobScheduler)getApplicationContext() .getSystemService(JOB_SCHEDULER_SERVICE);
        }
        SharedPreferences sharedPreferences= Utilis.getSharedPref(this);

        long refresh_min = Long.parseLong(sharedPreferences.getString(Constants.PREF_KEY_REFRESH_RATE, String.valueOf(15)));

        ComponentName componentName = new ComponentName(this, RefreshJobService.class);
        JobInfo jobInfo;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobInfo.Builder builder = new JobInfo.Builder(RefreshJobService.JOBID, componentName)
                    .setPeriodic(refresh_min*60*1000) //this value will over ride with minimum 15 min in android >=N if it is less than 15 min
                    .setPersisted(true)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setRequiresBatteryNotLow(true);
            }

            jobInfo = builder.build();

            if (jobScheduler != null) {
                int schedule = jobScheduler.schedule(jobInfo);
                if (schedule== JobScheduler.RESULT_SUCCESS){
                    Log.d("JobMainActivity","Refresh Job scheduled");
                }
            }else {
                Log.d("JobMainActivity","Job scheduler is null");
            }
        }
    }


    @OnClick(R.id.imgBtn_add)
    public void addCity(){
        startActivity(new Intent(MainActivity.this,AddCityActivity.class));

    }
    @OnClick(R.id.imgBtn_setting)
    public void openSettings(){
        startActivity(new Intent(MainActivity.this,SettingsActivity.class));

    }

    @OnClick(R.id.rl_lastUpdate)
    public void showDetailsofToday(){
        Intent intent = new Intent(MainActivity.this,DetailsActivity.class);
        Log.d("onitem clicked", "title: "+tv_headerTitle.getText().toString());

        intent.putExtra("unixtime",String.valueOf(System.currentTimeMillis()/1000));
        intent.putExtra("location",tv_headerTitle.getText().toString());
        intent.putExtra("lat",String.valueOf(currentLatLng.latitude));
        intent.putExtra("lng",String.valueOf(currentLatLng.longitude));
        startActivity(intent);
    }
    @Override
    public void onItemClicked(View view, int position) {
        Intent intent = new Intent(MainActivity.this,DetailsActivity.class);
        Log.d("onitem clicked", "title: "+tv_headerTitle.getText().toString());

        intent.putExtra("unixtime",dailyForecastRVAdapter.getCityItem(position).getTime().toString());
        intent.putExtra("location",tv_headerTitle.getText().toString());
        intent.putExtra("lat",String.valueOf(currentLatLng.latitude));
        intent.putExtra("lng",String.valueOf(currentLatLng.longitude));
        startActivity(intent);
    }

    private void buildLocationRequest() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
            return;

        }

        if(!isLocationEnabled) {
            requestEnableLocation();
            return;
        }

        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(location -> {

            if (location != null) {

                currentLatLng=new LatLng(location.getLatitude(),location.getLongitude());

                updateWeatherData();
            }else {
                Toast.makeText(getApplicationContext(),"Failed to get location - Try again",Toast.LENGTH_SHORT).show();
            }
        });
        task.addOnFailureListener(e -> Toast.makeText(getApplicationContext(),"Failed to get location",Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                buildLocationRequest();
            } else {
                // Permission was denied. Display an error message.
                Toast.makeText(getApplicationContext(), "Location permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }



    private void requestEnableLocation() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setNumUpdates(1);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        Task<LocationSettingsResponse> task = LocationServices.getSettingsClient(MainActivity.this).checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, locationSettingsResponse -> {
            // All location settings are satisfied. The client can initialize
            // location requests here.

            isLocationEnabled=true;
            buildLocationRequest();


        });

        task.addOnFailureListener(this, e -> {
            isLocationEnabled=false;
            if (e instanceof ResolvableApiException) {

                try {

                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(MainActivity.this,
                            PERMISSION_REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {

                    Toast.makeText(MainActivity.this,"Please turn on location manually",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==PERMISSION_REQUEST_CHECK_SETTINGS){
            Log.d("Check setting","");
            if (resultCode==RESULT_OK){
                buildLocationRequest();
            }else {
                Toast.makeText(MainActivity.this,"Please turn on location",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
