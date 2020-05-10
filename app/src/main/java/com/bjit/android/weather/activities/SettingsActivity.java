package com.bjit.android.weather.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import com.bjit.android.weather.Constants;
import com.bjit.android.weather.R;
import com.bjit.android.weather.Utilis;
import com.bjit.android.weather.app.App;
import com.bjit.android.weather.widgets.AppWidget;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId()==android.R.id.home){
            finish();
        }
        return true;
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener{


        boolean isEnabled=true;
        EditTextPreference min_temp;
        EditTextPreference max_temp;
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            ListPreference refresh_rate = findPreference("refresh_rate");
            ListPreference temp_unit = findPreference("temp_unit");
            SwitchPreferenceCompat notification = findPreference("notification");
            min_temp = findPreference("min_temp");
            max_temp = findPreference("max_temp");



            if (refresh_rate != null) {
                refresh_rate.setOnPreferenceChangeListener(this);
                refresh_rate.setDefaultValue(15);
            }
            if (temp_unit != null) {
                temp_unit.setOnPreferenceChangeListener(this);
                temp_unit.setDefaultValue("\u2103");
            }
            if (notification != null) {
                notification.setOnPreferenceChangeListener(this);
                notification.setDefaultValue(true);
                isEnabled = PreferenceManager
                        .getDefaultSharedPreferences(notification.getContext())
                        .getBoolean(notification.getKey(), false);
            }
            if (min_temp != null) {
                min_temp.setDefaultValue(15);
                min_temp.setOnPreferenceChangeListener(this);
                min_temp.setEnabled(isEnabled);
                min_temp.setOnBindEditTextListener(editText ->
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER)

                );


            }
            if (max_temp != null) {
                max_temp.setDefaultValue(32);
                max_temp.setEnabled(isEnabled);
                max_temp.setOnPreferenceChangeListener(this);
                max_temp.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER));
            }
        }


        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            SharedPreferences sharedPref = Utilis.getSharedPref(Objects.requireNonNull(getContext()));


            if (preference.getKey().equalsIgnoreCase("min_temp")){
                String max  = sharedPref.getString(Constants.PREF_KEY_MAX_TEMP, "32");
                if (Integer.parseInt(max)<Integer.parseInt((String) newValue)){

                    Toast.makeText(getContext(),"Value can not be greater than max temp",Toast.LENGTH_SHORT).show();
                    return false;
                }

            }
            if (preference.getKey().equalsIgnoreCase("max_temp")){

                String min  = sharedPref.getString(Constants.PREF_KEY_MIN_TEMP, "15");
                if (Integer.parseInt(min)>Integer.parseInt((String) newValue)){

                    Toast.makeText(getContext(),"Value can not be less than min temp",Toast.LENGTH_SHORT).show();
                    return false;
                }
            }


            Log.e("Setting Pref","Pref= "+preference.getKey()+" value= "+newValue);
            if (preference.getKey().equalsIgnoreCase("notification") &&  ((boolean)newValue)){
                if (max_temp!=null)
                    max_temp.setEnabled(true);
                if (min_temp!=null)
                    min_temp.setEnabled(true);
                Utilis.notificationJobSchedule();
            }else if (preference.getKey().equalsIgnoreCase("notification") && !((boolean)newValue)){
                if (max_temp!=null)
                    max_temp.setEnabled( false);
                if (min_temp!=null)
                    min_temp.setEnabled(false);
                Utilis.NotificationjobCancel();
            }

            if (preference.getKey().equalsIgnoreCase("temp_unit")){
                Log.e("Setting Pref","Broadcasting update");
                App.context.sendBroadcast(new Intent(getContext(), AppWidget.class).setAction(Constants.ACTION_WIDGET_UPDATE));
            }

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(preference.getKey(),newValue.toString());
            editor.apply();


            return true;
        }

    }
}