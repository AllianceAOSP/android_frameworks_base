/*
 * Copyright (C) 2016 AllianceROM, ~Morningstar
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
 * limitations under the License.
 */

package com.android.internal.util.alliance;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.internal.R;
import com.android.internal.util.AllianceUtils;

/**
 * @hide
 */
public class ShutdownDialog extends Dialog {

    private static final int ACTION_UPDATE = 0;
    private static final int ACTION_FACTORY_RESET = 1;
    private static final int ACTION_REBOOT = 2;
    private static final int ACTION_SHUTDOWN = 3;

    private final Context mContext;

    private TextView mPrimaryText;

    private ImageView mLogo;
    private ImageView mLogoShadow;

    public static ShutdownDialog create(Context context, int action) {
        return create(context,  WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG, action);
    }

    public static ShutdownDialog create(Context context, int windowType, int action) {
        final int theme = com.android.internal.R.style.Theme_Material_Light;
        return new ShutdownDialog(context, theme, windowType, action);
    }

    private ShutdownDialog(Context context, int themeResId, int windowType, int action) {
        super(context, themeResId);
        mContext = context;

        final LayoutInflater inflater = LayoutInflater.from(context);
        final View rootView = inflater.inflate(com.android.internal.R.layout.shutdown_layout, null, false);
        mLogo = (ImageView) rootView.findViewById(R.id.shutdown_logo);
        mLogoShadow = (ImageView) rootView.findViewById(R.id.shutdown_logo_shadow);
        mPrimaryText = (TextView) rootView.findViewById(R.id.shutdown_message);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(rootView);

        if (windowType != 0) {
            getWindow().setType(windowType);
        }
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final WindowManager.LayoutParams lp = getWindow().getAttributes();
        // turn off button lights while shutdown/reboot screen is showing
        lp.buttonBrightness = 0;
        lp.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR;
        getWindow().setAttributes(lp);
        setCancelable(false);
        setMessage(action);
        show();

        rootView.post(new Runnable() {
            @Override public void run() {
                // start the marquee
                mPrimaryText.setSelected(true);
            }
        });
        startLogoAnimation();
    }

    private void setMessage(int action) {
        String mMessage = "";

        switch (action) {
            case ACTION_UPDATE:
                mMessage = mContext.getResources().getString(com.android.internal.R.string.reboot_to_update_prepare);
                break;
            case ACTION_FACTORY_RESET:
                mMessage = mContext.getResources().getString(com.android.internal.R.string.reboot_progress);
                break;
            case ACTION_REBOOT:
            default:
                mMessage = mContext.getResources().getString(com.android.internal.R.string.reboot_progress);
                break;
            case ACTION_SHUTDOWN:
                mMessage = mContext.getResources().getString(com.android.internal.R.string.shutdown_progress);
                break;
        }
        mPrimaryText.setText(mMessage);
    }

    public void setMessage(final CharSequence msg) {
        mPrimaryText.setText(msg);
    }

    // This dialog will consume all events coming in to it
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

    @Override
    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent event) {
        return true;
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        return true;
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        return true;
    }

    private void startLogoAnimation() {
        setInitialState();

        final AnimationSet shadowSet = new AnimationSet(false);
        shadowSet.setRepeatCount(Animation.INFINITE);
        shadowSet.setRepeatMode(Animation.REVERSE);

        Animation alphaAnim = new AlphaAnimation(1.0f, 0.5f);
        alphaAnim.setDuration(2500);
        alphaAnim.setInterpolator(new LinearInterpolator());
        alphaAnim.setRepeatCount(Animation.INFINITE);
        alphaAnim.setRepeatMode(Animation.REVERSE);
        shadowSet.addAnimation(alphaAnim);

        Animation scaleAnim = new ScaleAnimation(1f, 0.7f, 1f, 0.7f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1.0f);
        scaleAnim.setDuration(2500);
        scaleAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleAnim.setRepeatCount(Animation.INFINITE);
        scaleAnim.setRepeatMode(Animation.REVERSE);
        shadowSet.addAnimation(scaleAnim);

        final Animation transAnim = new TranslateAnimation(0, 0, 0, -AllianceUtils.dpToPx(mContext, 30));
        transAnim.setDuration(2500);
        transAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        transAnim.setRepeatMode(Animation.REVERSE);
        transAnim.setRepeatCount(Animation.INFINITE);

        AnimationSet dropShadowSet = new AnimationSet(true);
        dropShadowSet.setInterpolator(new BounceInterpolator());
        dropShadowSet.setFillAfter(true);

        Animation dropAlpha = new AlphaAnimation(0.0f, 1.0f);
        dropAlpha.setDuration(2000);
        dropShadowSet.addAnimation(dropAlpha);

        Animation dropScale = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1.0f);
        dropScale.setDuration(2000);
        dropShadowSet.addAnimation(dropScale);

        Animation dropAnim = new TranslateAnimation(0, 0, -AllianceUtils.dpToPx(mContext, 300), 0);
        dropAnim.setDuration(2000);
        dropAnim.setInterpolator(new BounceInterpolator());
        dropAnim.setFillAfter(true);
        dropAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //nothing
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                mLogoShadow.startAnimation(shadowSet);
                mLogo.startAnimation(transAnim);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
                //nothing
            }
        });

        mLogo.startAnimation(dropAnim);
        mLogoShadow.startAnimation(dropShadowSet);
    }

    private void setInitialState() {
        Animation alphaAnim = new AlphaAnimation(1.0f, 0.0f);
        alphaAnim.setDuration(0);
        alphaAnim.setFillAfter(true);

        Animation transAnim = new TranslateAnimation(0, 0, 0, -AllianceUtils.dpToPx(mContext, 300));
        transAnim.setDuration(0);
        transAnim.setFillAfter(true);

        Animation scaleAnim = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1.0f);
        scaleAnim.setDuration(0);
        scaleAnim.setFillAfter(true);

        AnimationSet shadowSet = new AnimationSet(true);
        shadowSet.addAnimation(alphaAnim);
        shadowSet.addAnimation(scaleAnim);

        mLogo.startAnimation(transAnim);
        mLogoShadow.startAnimation(shadowSet);
    }
}
