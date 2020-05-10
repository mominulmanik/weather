package com.bjit.android.weather.interfaces;

import android.view.View;
import android.widget.CompoundButton;

public interface RVClickListener {
    public void onItemClicked(View view, int position);
    default public void onItemLongClickListener(View view, int position){};
}
