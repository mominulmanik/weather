package com.bjit.android.weather.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bjit.android.weather.R;
import com.bjit.android.weather.Utilis;
import com.bjit.android.weather.interfaces.RVClickListener;
import com.bjit.android.weather.retrofit.responseModel.EachHour;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HourlyForecastRVAdapter extends RecyclerView.Adapter<HourlyForecastRVAdapter.ViewHolder>{
    private List<EachHour> lists=new ArrayList<>();
    private Context context;
    private RVClickListener rvClickListener;

    public HourlyForecastRVAdapter(Context context, RVClickListener rvClickListener) {
        this.rvClickListener = rvClickListener;
        this.context = context;
    }


    public void setLists(List<EachHour> lists) {
        if (lists.size()>=25) {
            lists.subList(24, lists.size()).clear();
        }

        this.lists = lists;
    }
    public void addCity(EachHour city){
        lists.add(city);

    }

    @NonNull
    @Override
    public HourlyForecastRVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_24_hours_forecast, parent, false));


    }



    @Override
    public void onBindViewHolder(@NonNull HourlyForecastRVAdapter.ViewHolder holder, int position) {
        EachHour eachHour = lists.get(position);
        Date date = new java.util.Date(eachHour.getTime()*1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a",Locale.getDefault());
        String formattedDate = sdf.format(date);
        holder.temp_24.setText(Utilis.formatTemperature(context,eachHour.getTemperature()));
        holder.time_24.setText(formattedDate);
        //Log.d(TAG, "onBindViewHolder: "+formattedDate);
        //Log.d(TAG, "onBindViewHolder: "+eachHour.getTime());
        int imageForIcon = Utilis.getImageForIcon(eachHour.getIcon());
        if (imageForIcon!=-1)
            holder.icon_24.setImageResource(imageForIcon);



    }


    @Override
    public int getItemCount() {
        return lists.size();
    }

    public EachHour getItem(int position){
        return lists.get(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        @BindView(R.id.tvTime_24)
        AppCompatTextView time_24;
        @BindView(R.id.tv_temp_24)
        AppCompatTextView temp_24;
        @BindView(R.id.ivIcon_24)
        AppCompatImageView icon_24;



        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            if (rvClickListener!=null)
                rvClickListener.onItemClicked(v,this.getAdapterPosition());

        }
    }


}
