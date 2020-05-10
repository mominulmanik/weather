package com.bjit.android.weather.asyncTasks;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;
import com.bjit.android.weather.database.DatabaseClient;
import com.bjit.android.weather.model.City;

public class SaveToDB extends AsyncTask<Void, Void, Integer> {
    private Context context;
    private City city;


    public SaveToDB(Context context, City city) {
        this.context = context;
        this.city = city;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(Void... voids) {



        //adding to database
        if(context!=null) {
            DatabaseClient.getInstance(context).getAppDatabase()
                    .cityDao()
                    .insert(city);
            return 1;
        }
        return 0;
    }

    @Override
    protected void onPostExecute(Integer b) {
        super.onPostExecute(b);
    }
}
