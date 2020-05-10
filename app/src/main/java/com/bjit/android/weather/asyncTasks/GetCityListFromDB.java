package com.bjit.android.weather.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.bjit.android.weather.adapters.CityListRVAdapter;
import com.bjit.android.weather.database.DatabaseClient;
import com.bjit.android.weather.model.City;

import java.util.List;

public class GetCityListFromDB extends AsyncTask<Void, Void, List<City>> {

    private Context context;
    private CityListRVAdapter cityListRVAdapter;



    public GetCityListFromDB(Context context, CityListRVAdapter cityListRVAdapter) {
        this.context = context;
        this.cityListRVAdapter=cityListRVAdapter;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected List<City> doInBackground(Void... voids) {
        if(context!=null) {
            return DatabaseClient
                    .getInstance(context)
                    .getAppDatabase()
                    .cityDao()
                    .getAll();
        }else
            return null;
    }

    @Override
    protected void onPostExecute(List<City> tasks) {
        super.onPostExecute(tasks);
        cityListRVAdapter.setLists(tasks);
        cityListRVAdapter.notifyDataSetChanged();
        Log.d("GEtListFromDb","Size= "+cityListRVAdapter.getItemCount());

    }
}
