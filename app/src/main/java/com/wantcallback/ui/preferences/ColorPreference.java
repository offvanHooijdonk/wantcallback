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
    private static final int DIALOG_COLUMNS = 4;

    private static int[] LED_COLORS_RESOURCES = {R.color.led_blue, R.color.led_light_blue, R.color.led_red,
            R.color.led_pink, R.color.led_purple, R.color.led_cyan, R.color.led_teal,
            R.color.led_green, R.color.led_yellow, R.color.led_orange, R.color.led_white};

    private static int[] colors = null;

    public ColorPreference(final Context context) {
        super(context);

        initColorsList(context);
    }

    public ColorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        initColorsList(context);
    }

    private void initColorsList(Context ctx) {
        if (colors == null) {
            colors = new int[LED_COLORS_RESOURCES.length];
            int i = 0;
            for (int c : LED_COLORS_RESOURCES) {
                colors[i] = ctx.getResources().getColor(c);
                i++;
            }
        }
    }

    private void initPreference(final Context ctx) {
        setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                int currentValue = getPreferenceManager().getSharedPreferences().getInt(getKey(), 255);
                final ColorPickerDialog dialog = new ColorPickerDialog();
                dialog.setColors(colors, currentValue);
                dialog.setArguments(R.string.led_color_title, DIALOG_COLUMNS, ColorPickerDialog.SIZE_SMALL);
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

        initPreference(getContext());

        return super.onCreateView(parent);
    }
}
