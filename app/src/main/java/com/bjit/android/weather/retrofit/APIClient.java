package com.bjit.android.weather.retrofit;


import com.bjit.android.weather.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {

    private static Retrofit retrofit = null;
    private APIClient(){

    }

    public static Retrofit getClient() {

        LoggingInterceptor interceptor=new LoggingInterceptor();
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build();



        if(retrofit==null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.dark_sky_baseurl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }

}
