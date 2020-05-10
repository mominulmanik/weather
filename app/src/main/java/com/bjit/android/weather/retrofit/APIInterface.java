package com.bjit.android.weather.retrofit;

import com.bjit.android.weather.retrofit.responseModel.TodaysWeatherResponse;
import com.bjit.android.weather.retrofit.responseModel.pollution.PollutionResponse;
import com.bjit.android.weather.retrofit.timeSpecificResponse.TimeSpecificResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface APIInterface {
    @GET
    Call<TodaysWeatherResponse> getTodaysWeather(@Url String url);

    @GET
    Call<TimeSpecificResponse> getSpecificTimeWeather(@Url String url);

    @GET
    Call<PollutionResponse> getPollutedCityNearMe(@Url String url);
}
