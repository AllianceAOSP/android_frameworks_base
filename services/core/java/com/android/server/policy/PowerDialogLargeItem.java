package com.android.server.policy;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.internal.R;

/**
 * Created by morningstar on 4/5/16.
 */
public class PowerDialogLargeItem extends RelativeLayout {

    private TextView mTitle;
    private OnClickListener mClickListener;

    public PowerDialogLargeItem(Context context) {
        super(context);
        init(context);
    }

    public PowerDialogLargeItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PowerDialogLargeItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setClickable(true);
        this.setEnabled(true);
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater mInflater = LayoutInflater.from(context);
        mInflater.inflate(R.layout.power_dialog_large_item, this, true);
        mTitle = (TextView) findViewById(R.id.power_dialog_item_title);
    }

    public void setPrimaryText(String text) {
        mTitle.setText(text);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mClickListener != null) {
                mClickListener.onClick(this);
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP && (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER
                || event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
            if (mClickListener != null) {
                mClickListener.onClick(this);
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public void setOnClickListener(OnClickListener listener) {
        mClickListener = listener;
    }
}
