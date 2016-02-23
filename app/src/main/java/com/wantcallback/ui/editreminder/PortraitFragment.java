package com.wantcallback.ui.editreminder;

import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.wantcallback.R;
import com.wantcallback.helper.AppHelper;
import com.wantcallback.model.ContactInfo;

/**
 * Created by Yahor_Fralou on 2/19/2016.
 */
public class PortraitFragment extends Fragment {

    private ImageView photoInToolbar;
    private View photoOverlay;

    private ContactInfo contact;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fr_portrait_on_form, container);

        photoInToolbar = (ImageView) getActivity().findViewById(R.id.photoInToolbar);
        setImageMeasures(photoInToolbar);
        photoOverlay = getActivity().findViewById(R.id.photo_touch_intercept_overlay);

        photoOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (contact != null) {
                    startActivity(AppHelper.Intents.createContactIntent(contact.getId()));
                }
            }
        });

        return v;
    }

    private void setImageMeasures(ImageView iv) {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            DisplayMetrics outMetrics = new DisplayMetrics();
            display.getMetrics(outMetrics);

            int width = outMetrics.widthPixels;
            iv.getLayoutParams().height = width * 9 / 16;
        } else {
            iv.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            iv.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
        }
    }
}
