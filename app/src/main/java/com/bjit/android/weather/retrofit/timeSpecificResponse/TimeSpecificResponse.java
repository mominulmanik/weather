
package com.bjit.android.weather.retrofit.timeSpecificResponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TimeSpecificResponse {

    @SerializedName("latitude")
    @Expose
    private Double latitude;
    @SerializedName("longitude")
    @Expose
    private Double longitude;
    @SerializedName("timezone")
    @Expose
    private String timezone;
    @SerializedName("hourly")
    @Expose
    private SpecificHourly hourly;
    @SerializedName("daily")
    @Expose
    private SpecificDaily daily;
    @SerializedName("offset")
    @Expose
    private Integer offset;

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public SpecificHourly getHourly() {
        return hourly;
    }

    public void setHourly(SpecificHourly hourly) {
        this.hourly = hourly;
    }

    public SpecificDaily getDaily() {
        return daily;
    }

    public void setDaily(SpecificDaily daily) {
        this.daily = daily;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

}
