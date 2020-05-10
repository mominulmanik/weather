package com.bjit.android.weather.model;

import androidx.annotation.NonNull;

import java.util.Date;

public class PointModel {
    private float min,max;
    private String xAxisLabel;

    public PointModel(float min,float max,String label) {
        this.min=min;
        this.max=max;
        this.xAxisLabel=label;
    }

    public float getMin() {
        return min;
    }

    public void setMin(float min) {
        this.min = min;
    }

    public float getMax() {
        return max;
    }

    public void setMax(float max) {
        this.max = max;
    }

    String getXAxisLabel() {
        return xAxisLabel;
    }

    public void setXAxisLabel(String xAxisLabel) {
        this.xAxisLabel = xAxisLabel;
    }


}
