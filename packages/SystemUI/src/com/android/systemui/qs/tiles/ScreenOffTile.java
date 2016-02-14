package com.android.systemui.qs.tiles;

import android.content.Context;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.hardware.input.InputManager;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;

import com.android.internal.logging.MetricsConstants;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;

/** Quick settings tile: Screen off **/
public class ScreenOffTile extends QSTile<QSTile.BooleanState> {

    private PowerManager mPm;
    private boolean mListening;

    public ScreenOffTile(Host host) {
        super(host);
        mPm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
    }

    @Override
    protected BooleanState newTileState() {
        return new BooleanState();
    }

    public void setListening(boolean listening) {
        if (mListening == listening) return;
        mListening = listening;
    }

    @Override
    public void handleClick() {
        mHost.collapsePanels();
        mPm.goToSleep(SystemClock.uptimeMillis());
    }

    @Override
    protected void handleSecondaryClick() {
        mHost.collapsePanels();
        triggerVirtualKeypress(KeyEvent.KEYCODE_POWER, true);
    }

    @Override
    public void handleLongClick() {
        mHost.collapsePanels();
        triggerVirtualKeypress(KeyEvent.KEYCODE_POWER, true);
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.visible = true;
        state.label = mContext.getString(R.string.quick_settings_screen_off_label);
        state.contentDescription = mContext.getString(
                R.string.accessibility_quick_settings_screen_off);
        state.icon = ResourceIcon.get(R.drawable.ic_qs_power);
    }

    private void triggerVirtualKeypress(final int keyCode, final boolean longPress) {
        new Thread(new Runnable() {
            public void run() {
                InputManager im = InputManager.getInstance();
                KeyEvent keyEvent;
                if (longPress) {
                    keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
                    keyEvent.changeFlags(keyEvent, KeyEvent.FLAG_FROM_SYSTEM | KeyEvent.FLAG_LONG_PRESS);
                } else {
                    keyEvent = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
                    keyEvent.changeFlags(keyEvent, KeyEvent.FLAG_FROM_SYSTEM);
                }
                im.injectInputEvent(keyEvent, InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_RESULT);
            }
        }).start();
    }
}