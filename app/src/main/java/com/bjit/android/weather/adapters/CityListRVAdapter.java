package com.bjit.android.weather.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bjit.android.weather.R;
import com.bjit.android.weather.interfaces.RVClickListener;
import com.bjit.android.weather.model.City;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CityListRVAdapter extends RecyclerView.Adapter<CityListRVAdapter.ViewHolder>{
    private List<City> lists=new ArrayList<>();
    private RVClickListener rvClickListener;
    private Context context;

    public CityListRVAdapter(Context context, RVClickListener rvClickListener) {
        this.rvClickListener = rvClickListener;
        this.context=context;
    }


    public void setLists(List<City> lists) {
        this.lists = lists;
    }
    public void addCity(City city){
        lists.add(city);

    }

    @NonNull
    @Override
    public CityListRVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_city_overview, parent, false));


    }



    @Override
    public void onBindViewHolder(@NonNull CityListRVAdapter.ViewHolder holder, int position) {
        City city = lists.get(position);
        holder.tv_cityName.setText(city.getAddress());


    }


    @Override
    public int getItemCount() {
        return lists.size();
    }

    public City getCityItem(int position){
        return lists.get(position);
    }

    public void removeItem(int position) {
        lists.remove(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        @BindView(R.id.tv_cityName)
        AppCompatTextView tv_cityName;
        @BindView(R.id.rl_cityrootview)
        RelativeLayout rootView;



        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
            rootView.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View v) {
            if (rvClickListener!=null)
                rvClickListener.onItemClicked(v,this.getAdapterPosition());

        }

        @Override
        public boolean onLongClick(View v) {
            if (rvClickListener!=null )
                rvClickListener.onItemLongClickListener(v,this.getAdapterPosition());
            return true;
        }
    }


}
