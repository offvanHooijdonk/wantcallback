package com.wantcallback.ui.editreminder;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wantcallback.R;

/**
 * Created by Yahor_Fralou on 2/19/2016.
 */
public class PortraitFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fr_portrait_on_form, container);



        return v;
    }
}
