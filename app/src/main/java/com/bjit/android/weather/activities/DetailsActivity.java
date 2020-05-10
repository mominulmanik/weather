package com.bjit.android.weather.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bjit.android.weather.BuildConfig;
import com.bjit.android.weather.R;
import com.bjit.android.weather.Utilis;
import com.bjit.android.weather.adapters.TimeSpecificAdapter;
import com.bjit.android.weather.retrofit.APIClient;
import com.bjit.android.weather.retrofit.APIInterface;
import com.bjit.android.weather.retrofit.timeSpecificResponse.Datum_;
import com.bjit.android.weather.retrofit.timeSpecificResponse.EachHour;
import com.bjit.android.weather.retrofit.timeSpecificResponse.TimeSpecificResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailsActivity extends AppCompatActivity{

    @BindView(R.id.tv_headerTitle)
    AppCompatTextView tv_headerTitle;
    @BindView(R.id.imgBtn_add)
    ImageButton backButton;
    @BindView(R.id.imgBtn_setting)
    ImageButton settingsButton;
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.rv_hourlyForecast1)
    RecyclerView rv_hourlyForecast;
    @BindView(R.id.tv_sunrise)
    TextView sunrise;
    @BindView(R.id.tv_sunset)
    TextView sunset;
    @BindView(R.id.tv_feeling)
    TextView realFeel;
    @BindView(R.id.tv_dewpoint)
    TextView dewPoint;
    @BindView(R.id.tv_pressure)
    TextView pressure;
    @BindView(R.id.tv_uvindex)
    TextView uvIndex;
    @BindView(R.id.tv_date)
    TextView tvdate;
    @BindView(R.id.ll_containerLayout)
    LinearLayout ll_containerLayout;


    List<EachHour> lists;
    TimeSpecificAdapter timeSpecificAdapter;
    APIInterface apiInterface;
    String unixtime;
    String location;
    String lat,lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);

        backButton.setBackgroundResource(R.drawable.ic_back);
        settingsButton.setVisibility(View.INVISIBLE);
        tv_headerTitle.setCompoundDrawables(null, null, null, null);

        Intent intent = getIntent();
        if(intent!=null) {
            unixtime = intent.getStringExtra("unixtime");
            location = intent.getStringExtra("location");
            lat = intent.getStringExtra("lat");
            lng = intent.getStringExtra("lng");

            Date date = new Date(Integer.parseInt(unixtime)*1000L);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy",Locale.getDefault());
            String formattedDate = sdf.format(date);
            tvdate.setText(formattedDate);
        }

        if (location!=null && !location.isEmpty())
            tv_headerTitle.setText(location);
        else
            tv_headerTitle.setText(getResources().getString(R.string.default_header_title));
        lists = new ArrayList<>();


        timeSpecificAdapter = new TimeSpecificAdapter(getApplicationContext(), null);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.refresh_progress_1,
                R.color.refresh_progress_2,
                R.color.refresh_progress_3,
                R.color.refresh_progress_4);
        swipeRefreshLayout.setOnRefreshListener(this::getWeatherData);

        apiInterface = APIClient.getClient().create(APIInterface.class);
        ll_containerLayout.setVisibility(View.GONE);

        rv_hourlyForecast.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        rv_hourlyForecast.setAdapter(timeSpecificAdapter);
        DividerItemDecoration dividerItemDecorationHourly = new DividerItemDecoration(rv_hourlyForecast.getContext(),
                LinearLayoutManager.HORIZONTAL);
        dividerItemDecorationHourly.setDrawable(getResources().getDrawable(R.drawable.divider));
        rv_hourlyForecast.addItemDecoration(dividerItemDecorationHourly);
        getWeatherData();
    }

    private void getWeatherData() {


        swipeRefreshLayout.setRefreshing(true);
        if (!Utilis.isNetworkConnected(this)){
            Toast.makeText(getApplicationContext(),"No internet available",Toast.LENGTH_SHORT).show();
            return;
        }
        apiInterface.getSpecificTimeWeather("forecast/"+ BuildConfig.dark_sky_apikey +"/"+lat+","+lng+","+unixtime+"?units=si&exclude=currently,flags").enqueue(new Callback<TimeSpecificResponse>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(Call<TimeSpecificResponse> call, Response<TimeSpecificResponse> response) {

                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful()) {
                    ll_containerLayout.setVisibility(View.VISIBLE);
                    Datum_ daily = response.body().getDaily().getData().get(0);
                    Date sunriseTime = new java.util.Date(daily.getSunriseTime()*1000L);
                    Date sunSetTime = new java.util.Date(daily.getSunsetTime()*1000L);
                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                    String formattedSunriseTime = sdf.format(sunriseTime);
                    String formattedSunsetTime = sdf.format(sunSetTime);
                    timeSpecificAdapter.setLists(response.body().getHourly().getData());
                    timeSpecificAdapter.notifyDataSetChanged();
                    sunrise.setText(formattedSunriseTime);
                    sunset.setText(formattedSunsetTime);
                    pressure.setText(String.format(Locale.getDefault(),"%.02f", daily.getPressure()));
                    uvIndex.setText(String.format(Locale.getDefault(),"%.02f", daily.getUvIndex()));
                    dewPoint.setText(String.format(Locale.getDefault(),"%.02f", daily.getDewPoint()));
                    realFeel.setText(Utilis.formatTemperature(DetailsActivity.this,daily.getApparentTemperatureHigh())
                            +" ~ "+Utilis.formatTemperature(DetailsActivity.this,daily.getApparentTemperatureLow()));
                }else{
                    Log.d("RESPONSE", "onResponseError: "+response.errorBody().toString());
                    Toast.makeText(getApplicationContext(),"Failed to get weather data",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TimeSpecificResponse> call, Throwable t) {
                Log.d("RESPONSE", "onFailure: "+ Objects.requireNonNull(t.getCause()).toString());
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getApplicationContext(),"Failed to get location",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @OnClick(R.id.imgBtn_add)
    public void goBack(){
        onBackPressed();
    }

}
