package com.wantcallback.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.EditText;

/**
 * Created by Yahor_Fralou on 8/21/2015.
 */
public class EditTextTrackFixed extends EditText {
    public EditTextTrackFixed(Context context) {
        super(context);
    }

    public EditTextTrackFixed(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextTrackFixed(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EditTextTrackFixed(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return new FixedInputConnection(super.onCreateInputConnection(outAttrs),
                true);
    }

    private class FixedInputConnection extends InputConnectionWrapper {

        public FixedInputConnection(InputConnection target, boolean mutable) {
            super(target, mutable);
        }

        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            if (beforeLength == 1 && afterLength == 0) {
                // backspace
                // Magic here: if trying to assign value in a single operation, ACTION_UP is not triggered
                boolean res = super.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                res = res & super.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
                return res;
            }

            return super.deleteSurroundingText(beforeLength, afterLength);
        }
    }
}
