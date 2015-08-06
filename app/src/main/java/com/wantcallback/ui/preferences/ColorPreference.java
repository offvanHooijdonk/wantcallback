package com.wantcallback.ui.preferences;

import android.app.Activity;
import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.android.colorpicker.ColorPickerDialog;
import com.android.colorpicker.ColorPickerSwatch;
import com.wantcallback.R;

/**
 * Created by Yahor_Fralou on 8/5/2015.
 */
public class ColorPreference extends Preference {

    public ColorPreference(final Context context) {
        super(context);

        /*init(context);*/
    }

    public ColorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        /*init(context);*/
    }

    private void init(final Context ctx) {
        setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                int currentValue = getPreferenceManager().getSharedPreferences().getInt(getKey(), 255);
                final ColorPickerDialog dialog = new ColorPickerDialog();
                dialog.setColors(new int[]{0x0000FF, 0xFF0000}, currentValue);
                dialog.setArguments(R.string.led_color_title, 3, 2);
                dialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int color) {
                        getPreferenceManager().getSharedPreferences().edit().putInt(getKey(), color).commit();
                        dialog.dismiss();
                    }
                });
                dialog.show(((Activity) ctx).getFragmentManager(), "LEDColorDialog");
                return true;
            }
        });
    }

    @Override
    protected View onCreateView(ViewGroup parent) {

        init(getContext());

        return super.onCreateView(parent);
    }
}
