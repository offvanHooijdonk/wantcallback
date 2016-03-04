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

import com.android.contacts.common.util.MaterialColorMapUtils;
import com.wantcallback.R;
import com.wantcallback.helper.AppHelper;
import com.wantcallback.helper.ColorHelper;
import com.wantcallback.model.ContactInfo;
import com.wantcallback.model.ReminderInfo;

/**
 * Created by Yahor_Fralou on 2/19/2016.
 */
public class PortraitFragment extends Fragment implements IColorableFragment {

    private ImageView photoInToolbar;
    private View photoOverlay;

    private ContactInfo contact;
    private ReminderInfo reminderInfo;
    private EditReminderActivity.MODE mode;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fr_portrait_on_form, container);

        photoInToolbar = (ImageView) getActivity().findViewById(R.id.photoInToolbar);
        setImageMeasures(photoInToolbar);
        photoOverlay = getActivity().findViewById(R.id.photo_touch_intercept_overlay);

        initForm();

        return v;
    }

    private void initForm() {
        if (mode == EditReminderActivity.MODE.BLANK) {

        } else if (mode == EditReminderActivity.MODE.CREATE) {

        } else if (mode == EditReminderActivity.MODE.EDIT) {

        }

        photoOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (contact != null) {
                    startActivity(AppHelper.Intents.buildContactIntent(contact.getId()));
                }
            }
        });
    }

    private void displayPortrait() {
        photoInToolbar.setImageURI(contact.getPhotoUri());
        photoInToolbar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        photoInToolbar.setImageAlpha(255);
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

    public void setContact(ContactInfo contact) {
        this.contact = contact;
    }

    public void setReminderInfo(ReminderInfo reminderInfo) {
        this.reminderInfo = reminderInfo;
    }

    public void setMode(EditReminderActivity.MODE mode) {
        this.mode = mode;
    }

    @Override
    public void colorUI(int color) {
        // TODO add colors

        if (contact == null || contact.getPhotoUri() == null || contact.getThumbUri() == null) {
            photoInToolbar.setScaleType(ImageView.ScaleType.CENTER);
            photoInToolbar.setImageResource(R.drawable.ic_person_white_188dp);
            photoInToolbar.setImageAlpha(96);

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (this.getView() != null) {
                    this.getView().setBackgroundColor(color);
                }
            }
        }

    }

    @Override
    public void colorUI() {
        MaterialColorMapUtils.MaterialPalette palette;
        if (contact != null) {
            palette = ColorHelper.getMaterialColorForPhoneOrName(getActivity(), contact.pickIdentifier());
        } else {
            palette = ColorHelper.getPaletteOnColor(getActivity(), getActivity().getResources().getColor(R.color.app_primary));
        }

        colorUI(palette.mPrimaryColor);
    }
}
