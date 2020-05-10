
package com.bjit.android.weather.retrofit.responseModel;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import com.bjit.android.weather.Utilis;
import com.bjit.android.weather.model.CustomGraphDataSet;
import com.bjit.android.weather.model.PointModel;
import com.github.mikephil.charting.data.Entry;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Daily {

    @SerializedName("summary")
    @Expose
    private String summary;
    @SerializedName("icon")
    @Expose
    private String icon;
    @SerializedName("data")
    @Expose
    private List<EachDay> data = null;

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<EachDay> getData() {
        return data;
    }

    public void setData(List<EachDay> data) {
        this.data = data;
    }


    public CustomGraphDataSet getPreparedGraphData(){
        List<PointModel> modelList=new ArrayList<>();
        for(EachDay d:data) {
            Log.d("CustomGraphdata","min temp "+d.getTemperatureMin()+" max temp "+d.getTemperatureMax());
            modelList.add(new PointModel(d.getTemperatureMin(),d.getTemperatureMax(), Utilis.getFormattedDate(d.getTime(),"EEE")));
            PointModel pointModel = modelList.get(modelList.size() - 1);
            Log.d("CustomGraphdata","after add tolist min temp "+pointModel.getMin()+" max temp "+pointModel.getMax());
        }
        return new CustomGraphDataSet(modelList);

    }


}
