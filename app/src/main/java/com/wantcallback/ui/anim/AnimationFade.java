package com.wantcallback.ui.anim;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

/**
 * Created by Yahor_Fralou on 8/7/2015.
 */
public class AnimationFade extends AlphaAnimation {
    public static final int DURATION_MILLIS = 300;

    private boolean fadeIn;
    private View view;

    public AnimationFade(View v, boolean in, float alpha) {
        super(in ? 0.0f : alpha, in ? alpha : 0.0f);

        this.fadeIn = in;
        this.view = v;

        setUp();
    }

    public void runAnimation() {
        if (view.getAnimation() != null) {
            view.getAnimation().cancel();
            view.getAnimation().reset();
        }

        view.startAnimation(this);
    }

    private void setUp() {
        setDuration(DURATION_MILLIS);

        setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (fadeIn) {
                    view.setVisibility(View.INVISIBLE);
                } else {
                    view.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                if (fadeIn) {
                    view.setVisibility(View.VISIBLE);
                } else {
                    view.setVisibility(View.GONE);
                }
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

}
