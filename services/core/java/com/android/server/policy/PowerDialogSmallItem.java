package com.android.server.policy;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.internal.R;

/**
 * Created by morningstar on 4/5/16.
 */
public class PowerDialogSmallItem extends RelativeLayout {

    private ImageView mIcon;
    private OnClickListener mClickListener;

    public PowerDialogSmallItem(Context context) {
        super(context);
        init(context);
    }

    public PowerDialogSmallItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PowerDialogSmallItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
        this.setClickable(true);
        this.setEnabled(true);
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
    }

    private void init(Context context) {
        LayoutInflater mInflater = LayoutInflater.from(context);
        mInflater.inflate(R.layout.power_dialog_small_item, this, true);
        mIcon = (ImageView) findViewById(R.id.power_dialog_item_icon);
    }

    public void setIcon(int iconResId) {
        mIcon.setImageResource(iconResId);
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
