package com.daisa.qreader;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class PreferencesActivity extends AppCompatActivity{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            //SettingsFragment customFragment = (SettingsFragment) getSupportFragmentManager().findFragmentById(R.id.settings);
            final Context context = getContext();

            Preference pref = this.findPreference("reset_presentation");
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    MaterialShowcaseView.resetAll(context);
                    AppShowcase.drawerShowcaseViewsShown = 0;
                    AppShowcase.presentShowcaseViewsShown = 0;
                    Toast.makeText(context, "Presentacion reseteada", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
        }
    }
}