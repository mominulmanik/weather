package com.bjit.android.weather.interfaces;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.bjit.android.weather.model.City;

import java.util.List;

@Dao
public interface CityDao {
    //this interface is for room database
    //DAO stands for data access object
    //this provide methods for accessing data from room database

    @Query("SELECT * FROM city")
    List<City> getAll();

    @Query("SELECT * FROM city WHERE id = :id")
    public City getCityById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(City city);

    @Delete
    void delete(City city);

    @Update
    void update(City city);
}
