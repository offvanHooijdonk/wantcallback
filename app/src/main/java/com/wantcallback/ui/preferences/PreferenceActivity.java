package com.wantcallback.ui.preferences;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.android.colorpicker.ColorPickerDialog;
import com.android.colorpicker.ColorPickerSwatch;
import com.wantcallback.R;
import com.wantcallback.helper.AppHelper;

/**
 * Created by Yahor_Fralou on 7/23/2015.
 */
public class PreferenceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.pref);

            Preference preference = getPreferenceScreen().findPreference(getResources().getString(R.string.led_color_key));
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final ColorPickerDialog dialog = new ColorPickerDialog();
                    dialog.setColors(new int[]{0x0000FF, 0xFF0000}, AppHelper.Pref.getLEDColor(getActivity()));
                    dialog.setArguments(R.string.led_color_title, 3, 2);
                    dialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {
                        @Override
                        public void onColorSelected(int color) {
                            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putInt(getResources().getString(R.string.led_color_key), color).commit();
                            dialog.dismiss();
                        }
                    });
                    dialog.show(getFragmentManager(), "LEDColorDialog");
                    return true;
                }
            });
        }
    }
}
