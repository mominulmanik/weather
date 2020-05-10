package com.bjit.android.weather.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.bjit.android.weather.interfaces.CityDao;
import com.bjit.android.weather.model.City;


@Database(entities = {City.class}, version = 1,exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    // define all the entities and the database version.
    public abstract CityDao cityDao();
}
