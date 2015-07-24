package com.wantcallback.ui.actionbar;

import android.content.Context;
import android.support.v4.view.ActionProvider;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;

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
        boolean isAppEnabled = AppHelper.isApplicationEnabled(ctx);
        CompoundButton componentView = createSwitchCompat(isAppEnabled);

        componentView.setOnClickListener(new CompoundButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = ((CompoundButton) v).isChecked();
                for (ToggleListener l : listeners) {
                    l.onStateChanged(isChecked);
                }
            }
        });

        return componentView;
    }

    public void addToggleListener(ToggleListener l) {
        listeners.add(l);
    }

    public interface ToggleListener {
        void onStateChanged(boolean isChecked);
    }

    private SwitchCompat createSwitchCompat(boolean isChecked) {
        SwitchCompat switchView = new SwitchCompat(ctx);

        if (isChecked) {
            switchView.setChecked(true);
        } else {
            switchView.setChecked(false);
        }

        return switchView;
    }
}
