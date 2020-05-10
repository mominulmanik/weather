package com.bjit.android.weather.activities;

import android.Manifest;
import android.animation.LayoutTransition;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bjit.android.weather.BuildConfig;
import com.bjit.android.weather.Constants;
import com.bjit.android.weather.R;
import com.bjit.android.weather.Utilis;
import com.bjit.android.weather.adapters.CityListRVAdapter;
import com.bjit.android.weather.asyncTasks.DeleteFromDB;
import com.bjit.android.weather.asyncTasks.GetCityListFromDB;
import com.bjit.android.weather.asyncTasks.SaveToDB;
import com.bjit.android.weather.interfaces.RVClickListener;
import com.bjit.android.weather.model.City;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddCityActivity extends AppCompatActivity implements RVClickListener {
    @BindView(R.id.rv_cityList)
    RecyclerView rv_cityList;
    @BindView(R.id.cl_search)
    ConstraintLayout cl_search;
    @BindView(R.id.imgv_fab)
    ImageView imgv_fab;
    @BindView(R.id.actv_search_box)
    AutoCompleteTextView actv_search_box;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    Context context;
    CityListRVAdapter cityListRVAdapter;
    boolean isExpanded =false;
    LatLng currentMarkerLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_city);
        ButterKnife.bind(this);
        context=this;

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        rv_cityList.setLayoutManager(new LinearLayoutManager(context));
        rv_cityList.setHasFixedSize(true);
        cityListRVAdapter=new CityListRVAdapter(context,this);
        rv_cityList.setAdapter(cityListRVAdapter);
        progressBar.setVisibility(View.GONE);
        LayoutTransition layoutTransition = cl_search.getLayoutTransition();
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);


        buildAutoCompleteAdapter();

    }

    @Override
    protected void onResume() {
        super.onResume();
        new GetCityListFromDB(context,cityListRVAdapter).execute();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        int itemId = item.getItemId();
        if ( itemId== android.R.id.home) {
            finish();

        }else if(itemId==R.id.fetch_current){
            SharedPreferences.Editor editor = Utilis.getSharedPref(context).edit();
            editor.putString(Constants.CITY_EXTRA_DATA,"");
            editor.apply();

            Intent intent=new Intent(AddCityActivity.this,MainActivity.class);
            intent.putExtra(Constants.CITY_EXTRA_DATA,"");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();

        }
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.add_city,menu);
        return true;
    }

    @Override
    public void onItemClicked(View view, int position) {

        City city = cityListRVAdapter.getCityItem(position);
        Gson gson=new Gson();
        String toJson = gson.toJson(city);
        SharedPreferences.Editor editor = Utilis.getSharedPref(context).edit();
        editor.putString(Constants.CITY_EXTRA_DATA,toJson);
        editor.apply();

        Intent intent=new Intent(AddCityActivity.this,MainActivity.class);
        intent.putExtra(Constants.CITY_EXTRA_DATA,toJson);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();

    }

    @Override
    public void onItemLongClickListener(View view, int position) {
        new DeleteFromDB(this,cityListRVAdapter.getCityItem(position)).execute();
        cityListRVAdapter.removeItem(position);
        cityListRVAdapter.notifyDataSetChanged();
    }
    @OnClick(R.id.imgv_fab)
    public void toggleExpandedSearchView(){
        if (!isExpanded) {

            ViewGroup.LayoutParams layoutParams = cl_search.getLayoutParams();
            layoutParams.width=ViewGroup.LayoutParams.MATCH_PARENT;
            cl_search.setLayoutParams(layoutParams);
            cl_search.requestLayout();
            imgv_fab.animate().rotationBy(45).setInterpolator(new LinearInterpolator());
            imgv_fab.requestLayout();
            actv_search_box.setVisibility(View.VISIBLE);
            actv_search_box.requestFocus();
            isExpanded =true;
        } else {

            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                final View windowToken = getCurrentFocus();
                if(windowToken!=null)
                    imm.hideSoftInputFromWindow(windowToken.getWindowToken(), 0);
            }
            ViewGroup.LayoutParams layoutParams = cl_search.getLayoutParams();
            layoutParams.width=ViewGroup.LayoutParams.WRAP_CONTENT;
            cl_search.setLayoutParams(layoutParams);
            cl_search.requestLayout();
            imgv_fab.animate().rotationBy(- 45).setInterpolator(new LinearInterpolator());
            imgv_fab.requestLayout();
            actv_search_box.setVisibility(View.GONE);
            actv_search_box.clearFocus();
            actv_search_box.setText("");
            isExpanded =false;

        }
    }


    private void buildAutoCompleteAdapter() {


        Places.initialize(getApplicationContext(), BuildConfig.google_maps_key);
        PlacesClient placesClient = Places.createClient(this);



        actv_search_box.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {


            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {


                AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();


                // Use the builder to create a FindAutocompletePredictionsRequest.
                FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                        .setTypeFilter(TypeFilter.CITIES)
                        .setSessionToken(token)
                        .setQuery(charSequence.toString())
                        .build();




                placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {

                    AutoCompleteTVAdapter adapter = new AutoCompleteTVAdapter(context, R.layout.item_place_suggestion_layout, response.getAutocompletePredictions());
                    actv_search_box.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }).addOnFailureListener((exception) -> {

                });

            }

            @Override
            public void afterTextChanged(Editable editable) {


            }
        });

        actv_search_box.setOnItemClickListener((parent, view, position, id) -> {


            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                final View windowToken = getCurrentFocus();
                if(windowToken!=null)
                    imm.hideSoftInputFromWindow(windowToken.getWindowToken(), 0);
            }
            actv_search_box.clearFocus();
            actv_search_box.setSelection(actv_search_box.getText().length());
            progressBar.setVisibility(View.VISIBLE);
            AutocompletePrediction item = (AutocompletePrediction) parent.getAdapter().getItem(position);
            String placeId;
            if (item != null) {
                placeId = item.getPlaceId();
                String selectedAddress=item.getFullText(null).toString();
                actv_search_box.setText(selectedAddress);


                List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,Place.Field.LAT_LNG,Place.Field.ADDRESS);
                FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields)
                        .build();

                // Add a listener to handle the response.
                placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                    Place place = response.getPlace();
                    currentMarkerLatLng=place.getLatLng();

                    City city=new City();
                    city.setAddress( selectedAddress);
                    city.setLat(currentMarkerLatLng.latitude);
                    city.setLng(currentMarkerLatLng.longitude);
                    cityListRVAdapter.addCity(city);
                    cityListRVAdapter.notifyDataSetChanged();
                    new SaveToDB(context,city).execute();
                    progressBar.setVisibility(View.GONE);
                    toggleExpandedSearchView();

                    Toast.makeText(getApplicationContext(), "Added", Toast.LENGTH_SHORT).show();

                }).addOnFailureListener((exception) -> {
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        Log.d("PlacePicker", Objects.requireNonNull(apiException.getMessage()));
                        Toast.makeText(getApplicationContext(), "Failed to get city information", Toast.LENGTH_SHORT).show();

                    }
                });

            }




        });
        actv_search_box.setOnClickListener(view -> {
            actv_search_box.setCursorVisible(true);
            actv_search_box.setSelection(actv_search_box.getText().toString().length());
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(AddCityActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        }


    }



    class  AutoCompleteTVAdapter extends ArrayAdapter<AutocompletePrediction> {
        Context mContext;
        int layoutResourceId;
        List<AutocompletePrediction> data;

        AutoCompleteTVAdapter(@NonNull Context context, int resource, @NonNull List<AutocompletePrediction> objects) {
            super(context, resource, objects);
            this.mContext=context;
            this.layoutResourceId=resource;
            this.data=objects;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Nullable
        @Override
        public AutocompletePrediction getItem(int position) {
            return data.get(position);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if(convertView==null){
                // inflate the layout
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(layoutResourceId, parent, false);
            }
            AutocompletePrediction item = data.get(position);
            TextView primary = convertView.findViewById(R.id.tv_primaryText);
            primary.setText(item.getPrimaryText(null));
            TextView secondary = convertView.findViewById(R.id.tv_secondaryText);
            secondary.setText(item.getSecondaryText(null));

            return convertView;
        }
    }
}
