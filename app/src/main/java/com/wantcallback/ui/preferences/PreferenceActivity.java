package com.wantcallback.ui.preferences;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.wantcallback.R;

/**
 * Created by Yahor_Fralou on 7/23/2015.
 */
public class PreferenceActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.pref);
        }
    }
}
