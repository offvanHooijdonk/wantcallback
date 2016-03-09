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
public class PortraitFragment extends Fragment implements IFormFragment {

    private ImageView photoInToolbar;
    private View photoOverlay;

    private ContactInfo contact;
    private ReminderInfo reminderInfo;
    private EditReminderActivity.MODE mode;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fr_portrait_on_form, container, false);

        photoInToolbar = (ImageView) v.findViewById(R.id.photoInToolbar);
        setImageMeasures(photoInToolbar);
        photoOverlay = v.findViewById(R.id.photo_touch_intercept_overlay);

        initForm();

        return v;
    }

    /**
     * Show portrait, set colors if need
     */
    @Override
    public void drawUI() {
        if (contact != null) {
            showContactPortrait();
        } else {
            MaterialColorMapUtils.MaterialPalette palette = ColorHelper.getPaletteOnColor(getActivity(), getActivity().getResources().getColor(R.color.app_primary));
            showDefaultPortrait(palette.mPrimaryColor);
        }
    }

    /**
     * Show controls, show portrait if any, set colors
     */
    private void initForm() {
        drawUI();

        /*if (mode == EditReminderActivity.MODE.BLANK) {
            // do nothing special
        } else if (mode == EditReminderActivity.MODE.CREATE) {
            showContactPortrait();
        } else if (mode == EditReminderActivity.MODE.EDIT) {
            showContactPortrait();
        }*/

        photoOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (contact != null) {
                    startActivity(AppHelper.Intents.buildContactIntent(contact.getId()));
                }
            }
        });
    }

    /**
     * if contact has an image - show it, else - show default image in correspondent color
     */
    private void showContactPortrait() {
        if (contact.getPhotoUri() != null) {
            photoInToolbar.setImageURI(contact.getPhotoUri());
            photoInToolbar.setScaleType(ImageView.ScaleType.CENTER_CROP);
            photoInToolbar.setImageAlpha(255);
        } else {
            MaterialColorMapUtils.MaterialPalette palette = ColorHelper.getMaterialColorForPhoneOrName(getActivity(), contact.pickIdentifier());
            showDefaultPortrait(palette.mPrimaryColor);
        }
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

    private void showDefaultPortrait(int color) {
        photoInToolbar.setScaleType(ImageView.ScaleType.CENTER);
        photoInToolbar.setImageResource(R.drawable.ic_person_white_188dp);
        photoInToolbar.setImageAlpha(96);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (this.getView() != null) {
                this.getView().setBackgroundColor(color);
            }
        }
    }

    public void showPortrait(boolean show) {
        photoInToolbar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
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

}
