package com.bjit.android.weather.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.bjit.android.weather.database.DatabaseClient;
import com.bjit.android.weather.model.City;

public class DeleteFromDB extends AsyncTask<Void, Void, Integer> {

    private Context context;
    private City city;


    public DeleteFromDB(Context context, City city) {
        this.context = context;
        this.city = city;
    }

    @Override
    protected Integer doInBackground(Void... voids) {

        if(context!=null) {
             DatabaseClient.getInstance(context).getAppDatabase()
                    .cityDao()
                    .delete(city);
             return 0;
        }
        return 1;
    }

    @Override
    protected void onPostExecute(Integer b) {
        super.onPostExecute(b);
        if(context!=null)
            Toast.makeText(context, "City removed", Toast.LENGTH_SHORT).show();

    }
}
