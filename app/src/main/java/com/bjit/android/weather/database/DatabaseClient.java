package com.bjit.android.weather.database;

import android.content.Context;

import androidx.room.Room;

public class DatabaseClient {
    private static DatabaseClient mInstance;
    private AppDatabase appDatabase;

    private DatabaseClient(Context mCtx) {
        //Creating AppDatabase’s object is expensive so we will create a single instance of it.

        //creating the app database with Room database builder
        //MyToDos is the name of the database
        String DATABASE_NAME = "cityDatabase";
        appDatabase = Room.databaseBuilder(mCtx, AppDatabase.class, DATABASE_NAME).build();
    }

    public static synchronized DatabaseClient getInstance(Context mCtx) {
        if (mInstance == null) {
            mInstance = new DatabaseClient(mCtx);
        }
        return mInstance;
    }

    public AppDatabase getAppDatabase() {
        return appDatabase;
    }
}
