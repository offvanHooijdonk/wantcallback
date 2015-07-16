package com.wantcallback.ui.actionbar;

import android.content.Context;
import android.view.ActionProvider;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.wantcallback.helper.AppHelper;
import com.wantcallback.util.AppLaunchUtil;

/**
 * Created by Yahor_Fralou on 7/15/2015.
 */
public class AppEnableActionProvider extends ActionProvider {
    private Context ctx;

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

        switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AppLaunchUtil util = new AppLaunchUtil(ctx);
                // TODO add AsyncTask, show progress?
                if (isChecked) {
                    util.startAllServices();
                } else {
                    util.stopAllServices();
                }
            }
        });

        return switchView;
    }
}
