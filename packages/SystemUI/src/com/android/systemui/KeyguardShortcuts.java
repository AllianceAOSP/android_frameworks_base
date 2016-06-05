/*
 * Copyright (C) 2016 AllianceROM, ~Morningstar
 * Shamelessly kanged from Omni
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.systemui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.HorizontalScrollView;

import com.android.systemui.R;
import com.android.systemui.statusbar.phone.ActivityStarter;
import com.android.systemui.statusbar.phone.KeyguardBottomAreaView;
import com.android.systemui.statusbar.phone.PhoneStatusBar;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class KeyguardShortcuts extends LinearLayout {

    static final String TAG = "KeyguardShortcuts";
    static final boolean DEBUG = true;

    private List<String> mShortcuts = new ArrayList<>();
    private LinearLayout mShortcutItems;
    private HorizontalScrollView mShortcutsView;
    private ActivityStarter mActivityStarter;
    private int mShortcutItemMargin = 2;
    private int mAppIconPadding;
    private ImageView mKeyguardShortcutTrigger;
    private int mShortcutIconSize;
    private KeyguardBottomAreaView mParent;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            post(new Runnable() {
                @Override
                public void run() {
                    updateSettings();
                }
            });
        }
    };

    public KeyguardShortcuts(Context context) {
        this(context, null);
    }

    public KeyguardShortcuts(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KeyguardShortcuts(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        mContext.registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mContext.unregisterReceiver(mReceiver);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mShortcutsView = (HorizontalScrollView) findViewById(R.id.shortcuts_list);
        mShortcutsView.setHorizontalScrollBarEnabled(false);
        mShortcutItems = (LinearLayout) findViewById(R.id.shortcut_items);
        mKeyguardShortcutTrigger = (ImageView) findViewById(R.id.shortcuts_trigger);
    }

    
}