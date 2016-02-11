package com.android.systemui.qs;

import android.annotation.Nullable;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.android.systemui.R;
import com.android.systemui.statusbar.phone.QSTileHost;
import com.android.systemui.statusbar.phone.SystemUIDialog;

public class QSSettings extends ScrollView {

    private QSTileHost mHost;

    private boolean mAdapterEditingState;

    public QSSettings(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setFillViewport(true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        findViewById(R.id.reset_tiles).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                initiateTileReset();
            }
        });
    }

    private void initiateTileReset() {
        final AlertDialog d = new AlertDialog.Builder(mContext)
                .setMessage(R.string.qs_tiles_reset_confirmation)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(com.android.internal.R.string.reset,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mHost.initiateReset();
                            }
                        }).create();
        SystemUIDialog.makeSystemUIDialog(d);
        d.show();
    }

    public void setHost(QSTileHost host) {
        mHost = host;
    }

    public boolean getAdapterEditingState() {
        return mAdapterEditingState;
    }

    public void setAdapterEditingState(boolean editing) {
        this.mAdapterEditingState = editing;
    }
}