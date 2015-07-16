package com.wantcallback.ui.actionbar;

import android.content.Context;
import android.view.ActionProvider;
import android.view.View;
import android.widget.Switch;

import com.wantcallback.helper.AppHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yahor_Fralou on 7/15/2015.
 */
public class AppEnableActionProvider extends ActionProvider {
    private Context ctx;

    private List<ToggleListener> listeners = new ArrayList<>();

    public AppEnableActionProvider(Context context) {
        super(context);

        this.ctx = context;
    }

    @Override
    public View onCreateActionView() {
        Switch switchView = new Switch(ctx);

        boolean isAppEnabled = AppHelper.isApplicationEnabled(ctx);

        if (isAppEnabled) {
            switchView.setChecked(true);
        } else {
            switchView.setChecked(false);
        }

        switchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = ((Switch) v).isChecked();
                for (ToggleListener l : listeners) {
                    l.onStateChanged(isChecked);
                }
            }
        });

        return switchView;
    }

    public void addToggleListener(ToggleListener l) {
        listeners.add(l);
    }

    public interface ToggleListener {
        void onStateChanged(boolean isChecked);
    }
}
