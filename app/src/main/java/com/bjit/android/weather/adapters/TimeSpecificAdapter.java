package com.bjit.android.weather.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TimeSpecificAdapter extends RecyclerView.Adapter<TimeSpecificAdapter.ViewHolder>{
    private List<com.bjit.android.weather.retrofit.timeSpecificResponse.EachHour> lists=new ArrayList<>();
    private Context context;
    private RVClickListener rvClickListener;
    private static final String TAG = "HourlyForecastRVAdapter";

    public TimeSpecificAdapter(Context context, RVClickListener rvClickListener) {
        this.rvClickListener = rvClickListener;
        this.context = context;
    }


    public void setLists(List<com.bjit.android.weather.retrofit.timeSpecificResponse.EachHour> lists) {
        this.lists = lists;
    }

    @NonNull
    @Override
    public TimeSpecificAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new TimeSpecificAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_24, parent, false));


    }



    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull TimeSpecificAdapter.ViewHolder holder, int position) {
        com.bjit.android.weather.retrofit.timeSpecificResponse.EachHour eachHour = lists.get(position);

        Date date = new Date(eachHour.getTime()*1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String formattedDate = sdf.format(date);
        int imageForIcon = Utilis.getImageForIcon(eachHour.getIcon());
        if (imageForIcon!=-1)
            holder.icon_24.setImageResource(imageForIcon);

        holder.temp_24.setText(Utilis.formatTemperature(context,eachHour.getTemperature()));
        holder.time_24.setText(formattedDate);
        holder.humidity.setText(""+eachHour.getHumidity().toString());
        holder.precipitation.setText(eachHour.getPrecipProbability().toString()+"%");
        holder.wind_speed.setText(eachHour.getWindSpeed().toString()+"km/hr");
        holder.cloud_cover.setText(eachHour.getCloudCover().toString()+"%");
        Log.d(TAG, "onBindViewHolder: "+formattedDate);
        Log.d(TAG, "onBindViewHolder: "+eachHour.getTime());



    }


    @Override
    public int getItemCount() {
        return lists.size();
    }

//    public EachHour getItem(int position){
//        return lists.get(position);
//    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        @BindView(R.id.tvTime_24)
        AppCompatTextView time_24;
        @BindView(R.id.tv_temp_24)
        AppCompatTextView temp_24;
        @BindView(R.id.ivIcon_24)
        AppCompatImageView icon_24;
        @BindView(R.id.tv_humidity)
        AppCompatTextView humidity;
        @BindView(R.id.tv_precipitation)
        AppCompatTextView precipitation;
        @BindView(R.id.tv_wind_speed)
        AppCompatTextView wind_speed;
        @BindView(R.id.tv_cloud_cover)
        AppCompatTextView cloud_cover;



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
