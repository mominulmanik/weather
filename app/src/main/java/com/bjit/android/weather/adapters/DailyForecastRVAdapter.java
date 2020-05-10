package com.bjit.android.weather.adapters;

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
import com.bjit.android.weather.retrofit.responseModel.EachDay;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DailyForecastRVAdapter extends RecyclerView.Adapter<DailyForecastRVAdapter.ViewHolder>{
    private List<EachDay> lists=new ArrayList<>();
    private Context context;
    private RVClickListener rvClickListener;
    private static final String TAG = "DailyForecastRVAdapter";

    public DailyForecastRVAdapter(Context context, RVClickListener rvClickListener) {
        this.rvClickListener = rvClickListener;
        this.context = context;
    }


    public void setLists(List<EachDay> lists) {
        this.lists = lists;
        Log.d(TAG, "setLists: "+lists);
    }
    public void addCity(EachDay city){
        lists.add(city);

    }

    @NonNull
    @Override
    public DailyForecastRVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.daily_forecast_item, parent, false));


    }



    @Override
    public void onBindViewHolder(@NonNull DailyForecastRVAdapter.ViewHolder holder, int position) {
        EachDay city = lists.get(position);


        Date date = new Date(city.getTime()*1000L);

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE",Locale.getDefault());

        String formattedDate = sdf.format(date);

        SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM",Locale.getDefault());
        String formattedDate1 = sdf1.format(date);


        holder.date.setText(formattedDate1);
        //holder.day.setText(""+city.getUvIndexTime());
        holder.day.setText(formattedDate);


        holder.max_temp.setText(Utilis.formatTemperature(context,city.getTemperatureMax()));
        holder.min_temp.setText(Utilis.formatTemperature(context,city.getTemperatureMin()));
        int imageForIcon = Utilis.getImageForIcon(city.getIcon());
        if (imageForIcon!=-1)
            holder.icon.setImageResource(imageForIcon);


    }


    @Override
    public int getItemCount() {
        return lists.size();
    }

    public EachDay getCityItem(int position){
        return lists.get(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        @BindView(R.id.tvTime_24)
        AppCompatTextView day;
        @BindView(R.id.tvDate)
        AppCompatTextView date;
        @BindView(R.id.ivIcon_24)
        AppCompatImageView icon;
        @BindView(R.id.tv_min_temp)
        AppCompatTextView min_temp;
        @BindView(R.id.tv_temp_24)
        AppCompatTextView max_temp;

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
