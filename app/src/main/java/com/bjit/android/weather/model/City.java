package com.bjit.android.weather.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class City implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private
    int id;
    @ColumnInfo(name = "address")
    private
    String address;
    @ColumnInfo(name = "lat")
    private
    double lat;
    @ColumnInfo(name = "lng")
    private
    double lng;

    public City() {
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }

        if (!(obj instanceof City)){
            return false;
        }

        City other = (City) obj;

        return id == other.id ;
    }
}
