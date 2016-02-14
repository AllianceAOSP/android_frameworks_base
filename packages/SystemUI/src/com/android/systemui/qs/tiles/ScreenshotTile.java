package com.android.systemui.qs.tiles;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.provider.Settings;
import android.os.RemoteException;
import android.view.View;

import com.android.internal.logging.MetricsConstants;
import com.android.systemui.R;
import com.android.systemui.screenshot.TakeScreenshotService;
import com.android.systemui.qs.QSTile;

/** Quick settings tile: Screenshot **/
public class ScreenshotTile extends QSTile<QSTile.BooleanState> {

    private boolean mListening;
    private final Object mScreenshotLock = new Object();
    private ServiceConnection mScreenshotConnection = null;

    public ScreenshotTile(Host host) {
        super(host);
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
        /* wait for the panel to close */
        try {
             Thread.sleep(1500);
        } catch (InterruptedException ie) {
             // Do nothing
        }
        takeScreenshot();
    }

    @Override
    protected void handleSecondaryClick() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.android.gallery3d",
            "com.android.gallery3d.app.GalleryActivity");
        mHost.startActivityDismissingKeyguard(intent);
    }

    @Override
    public void handleLongClick() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.android.gallery3d",
            "com.android.gallery3d.app.GalleryActivity");
        mHost.startActivityDismissingKeyguard(intent);
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.visible = true;
        state.label = mContext.getString(R.string.quick_settings_screenshot_label);
        state.contentDescription = mContext.getString(
                R.string.accessibility_quick_settings_screenshot);
        state.icon = ResourceIcon.get(R.drawable.ic_qs_screenshot);
    }

    /**private int screenshotDelay() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.SCREENSHOT_DELAY, 2);
    }**/

    final Runnable mScreenshotTimeout = new Runnable() {
        @Override
        public void run() {
            synchronized (mScreenshotLock) {
                if (mScreenshotConnection != null) {
                    mContext.unbindService(mScreenshotConnection);
                    mScreenshotConnection = null;
                }
            }
        }
    };

    private void takeScreenshot() {
        synchronized (mScreenshotLock) {
            if (mScreenshotConnection != null) {
                return;
            }

            Intent intent = new Intent(mContext, TakeScreenshotService.class);
            ServiceConnection conn = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    synchronized (mScreenshotLock) {
                        if (mScreenshotConnection != this) {
                            return;
                        }

                        Messenger messenger = new Messenger(service);
                        Message msg = Message.obtain(null, 1);
                        final ServiceConnection myConn = this;
                        Handler h = new Handler(mHandler.getLooper()) {
                            @Override
                            public void handleMessage(Message msg) {
                                synchronized (mScreenshotLock) {
                                    if (mScreenshotConnection == myConn) {
                                        mContext.unbindService(mScreenshotConnection);
                                        mScreenshotConnection = null;
                                        mHandler.removeCallbacks(mScreenshotTimeout);
                                    }
                                }
                            }
                        };
                        msg.replyTo = new Messenger(h);
                        msg.arg1 = msg.arg2 = 0;

                        // Take the screenshot
                        try {
                            messenger.send(msg);
                        } catch (RemoteException e) {
                            // Do nothing here
                        }
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    // Do nothing here
                }
            };

            if (mContext.bindService(intent, conn, mContext.BIND_AUTO_CREATE)) {
                mScreenshotConnection = conn;
                mHandler.postDelayed(mScreenshotTimeout, 10000);
            }
        }
    }
}