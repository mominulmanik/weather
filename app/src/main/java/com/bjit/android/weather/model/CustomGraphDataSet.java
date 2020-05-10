package com.bjit.android.weather.model;

import com.bjit.android.weather.retrofit.responseModel.EachDay;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

public class CustomGraphDataSet {

    private List<PointModel> modelList;



    public CustomGraphDataSet(List<PointModel> modelList) {
        this.modelList=modelList;
    }

    public List<Entry>getAllMinDataAsEntry(){
        List<Entry> dd=new ArrayList<>();
        if (modelList!=null){
            for(PointModel d:modelList)
                dd.add(new Entry(dd.size(),d.getMin()));
        }
        return dd;

    }
    public List<Entry>getAllMaxDataAsEntry(){
        List<Entry> dd=new ArrayList<>();
        if (modelList!=null){
            for(PointModel d:modelList)
                dd.add(new Entry(dd.size(),d.getMax()));
        }
        return dd;

    }
    public String getLabelForIndex(int index){
        return modelList.get(index).getXAxisLabel();

    }

    public List<PointModel> getModelList() {
        return modelList;
    }

    public void setModelList(List<PointModel> modelList) {
        this.modelList = modelList;
    }
}
