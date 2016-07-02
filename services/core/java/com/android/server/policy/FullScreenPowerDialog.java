package com.android.server.policy;

import android.app.ActivityManagerNative;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import com.android.internal.util.AllianceUtils;
import com.android.internal.util.FlashlightController;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.policy.PhoneWindowManager;
import com.android.server.policy.PowerDialogLargeItem;
import com.android.server.policy.PowerDialogSmallItem;

import com.android.internal.R;

import java.lang.Exception;
import java.lang.Process;
import java.lang.Runtime;

/**
 * Created by morningstar on 6/5/16.
 */
public class FullScreenPowerDialog extends Dialog {

    private static final String TAG = "FullScreenPowerDialog";

    private static final int DISMISS = 0;

    private int navBarHeight = 0;
    private int buttonHeight = 0;
    private int baseTranslationSmall = 0;
    private int baseTranslationLarge = 0;

    private final Context mContext;

    private PowerDialogLargeItem mPowerItem;
    private PowerDialogLargeItem mRebootItem;
    private PowerDialogLargeItem mRebootReboot;
    private PowerDialogLargeItem mRebootSoft;
    private PowerDialogLargeItem mRebootRecovery;
    private PowerDialogLargeItem mRebootBootloader;
    private PowerDialogLargeItem mRebootSystemUI;

    private PowerDialogSmallItem mAirplaneItem;
    private PowerDialogSmallItem mScreenshotItem;
    private PowerDialogSmallItem mSettingsItem;
    private PowerDialogSmallItem mFlashlightItem;
    private PowerDialogSmallItem mSearchItem;
    private PowerDialogSmallItem mVoiceItem;
    private PowerDialogSmallItem mLockItem;
    private PowerDialogSmallItem mAudioItem;

    private PowerDialogSmallItem mBackItem;

    private int[] clickableItems = { R.id.power_dialog_power_item,      R.id.power_dialog_reboot_item,     R.id.power_dialog_reboot_reboot,
                                     R.id.power_dialog_reboot_soft,     R.id.power_dialog_reboot_recovery, R.id.power_dialog_reboot_bootloader,
                                     R.id.power_dialog_reboot_systemui, R.id.power_dialog_airplane_item,   R.id.power_dialog_screenshot_item,
                                     R.id.power_dialog_settings_item,   R.id.power_dialog_flashlight_item, R.id.power_dialog_search_item,
                                     R.id.power_dialog_voice_item,      R.id.power_dialog_lock_item,       R.id.power_dialog_audio_item,
                                     R.id.power_dialog_back,            R.id.power_dialog_root };

    private Handler msgHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == DISMISS) {
                    dismissDialog();
                }
            }
        };

    // Make sure we close the dialog if the screen shuts off
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action) || Intent.ACTION_SCREEN_OFF.equals(action)) {
                    String reason = intent.getStringExtra(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY);
                    if (!PhoneWindowManager.SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS.equals(reason)) {
                        msgHandler.sendEmptyMessage(DISMISS);
                    }
                }
            }
        };

    private FlashlightController mFlashlightController = null;

    public static FullScreenPowerDialog create(Context context) {
        return create(context, WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
    }

    public static FullScreenPowerDialog create(Context context, int windowType) {
        final int theme = android.R.style.Theme_Material_Light;
        return new FullScreenPowerDialog(context, theme, windowType);
    }

    public FullScreenPowerDialog(Context context, int themeResId, int windowType) {
        super(context, themeResId);
        mContext = getContext();

        final LayoutInflater inflater = LayoutInflater.from(context);
        View dialogRoot = inflater.inflate(R.layout.full_screen_power_dialog, null, false);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mContext.registerReceiver(mReceiver, filter);

        mPowerItem = (PowerDialogLargeItem) dialogRoot.findViewById(R.id.power_dialog_power_item);
        mRebootItem = (PowerDialogLargeItem) dialogRoot.findViewById(R.id.power_dialog_reboot_item);
        mRebootReboot = (PowerDialogLargeItem) dialogRoot.findViewById(R.id.power_dialog_reboot_reboot);
        mRebootSoft = (PowerDialogLargeItem) dialogRoot.findViewById(R.id.power_dialog_reboot_soft);
        mRebootRecovery = (PowerDialogLargeItem) dialogRoot.findViewById(R.id.power_dialog_reboot_recovery);
        mRebootBootloader = (PowerDialogLargeItem) dialogRoot.findViewById(R.id.power_dialog_reboot_bootloader);
        mRebootSystemUI = (PowerDialogLargeItem) dialogRoot.findViewById(R.id.power_dialog_reboot_systemui);

        mAirplaneItem = (PowerDialogSmallItem) dialogRoot.findViewById(R.id.power_dialog_airplane_item);
        mScreenshotItem = (PowerDialogSmallItem) dialogRoot.findViewById(R.id.power_dialog_screenshot_item);
        mSettingsItem = (PowerDialogSmallItem) dialogRoot.findViewById(R.id.power_dialog_settings_item);
        mFlashlightItem = (PowerDialogSmallItem) dialogRoot.findViewById(R.id.power_dialog_flashlight_item);
        mSearchItem = (PowerDialogSmallItem) dialogRoot.findViewById(R.id.power_dialog_search_item);
        mVoiceItem = (PowerDialogSmallItem) dialogRoot.findViewById(R.id.power_dialog_voice_item);
        mLockItem = (PowerDialogSmallItem) dialogRoot.findViewById(R.id.power_dialog_lock_item);
        mAudioItem = (PowerDialogSmallItem) dialogRoot.findViewById(R.id.power_dialog_audio_item);
        mBackItem = (PowerDialogSmallItem) dialogRoot.findViewById(R.id.power_dialog_back);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(dialogRoot);

        if (windowType != 0) {
            getWindow().setType(windowType);
        }

        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final WindowManager.LayoutParams params = getWindow().getAttributes();
        params.buttonBrightness = 0;
        params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR;
        params.windowAnimations = R.style.FullscreenPowerMenuAnimation;
        getWindow().setAttributes(params);

        try {
            navBarHeight = Settings.System.getInt(mContext.getContentResolver(), Settings.System.NAVIGATION_BAR_HEIGHT);
        } catch (Settings.SettingNotFoundException e) {
            navBarHeight = 48; // Default nav bar height
            Log.e(TAG, "Error getting navbar height");
        }
        Settings.System.putInt(mContext.getContentResolver(), Settings.System.NAVIGATION_BAR_HEIGHT, 0);
        
        mFlashlightController = new FlashlightController(mContext);

        ClickHandler clickHandler = new ClickHandler();
        for (int i = 0; i < clickableItems.length; i++) {
            findViewById(clickableItems[i]).setOnClickListener(clickHandler);
        }

        setCancelable(true);
        initItems();
        show();
    }

    private void initItems() {
        buttonHeight = mPowerItem.getMeasuredHeight();
        baseTranslationSmall = AllianceUtils.dpToPx(mContext, 16);
        baseTranslationLarge = AllianceUtils.dpToPx(mContext, 50);
        Resources res = mContext.getResources();

        mPowerItem.setPrimaryText(res.getString(R.string.power_dialog_power_off));
        mRebootItem.setPrimaryText(res.getString(R.string.power_dialog_reboot));
        mRebootReboot.setPrimaryText(res.getString(R.string.power_dialog_reboot));
        mRebootSoft.setPrimaryText(res.getString(R.string.power_dialog_reboot_soft));
        mRebootRecovery.setPrimaryText(res.getString(R.string.power_dialog_reboot_recovery));
        mRebootBootloader.setPrimaryText(res.getString(R.string.power_dialog_reboot_bootloader));
        mRebootSystemUI.setPrimaryText(res.getString(R.string.power_dialog_reboot_systemui));

        mAirplaneItem.setIcon(AllianceUtils.isAirplaneModeOn(mContext) ? R.drawable.power_dialog_airplane_on : R.drawable.power_dialog_airplane_off);
        mFlashlightItem.setIcon(mFlashlightController.isEnabled() ? R.drawable.power_dialog_flashlight_on : R.drawable.power_dialog_flashlight_off);
        switch (AllianceUtils.getAudioMode(mContext)) {
            case 0: // normal
            default:
                mAudioItem.setIcon(R.drawable.power_dialog_ring);
                break;
            case 1: // silent
                mAudioItem.setIcon(R.drawable.power_dialog_silent);
                break;
            case 2: // vibrate
                mAudioItem.setIcon(R.drawable.power_dialog_vibrate);
                break;
        }

        mScreenshotItem.setIcon(R.drawable.power_dialog_screenshot);
        mSettingsItem.setIcon(R.drawable.power_dialog_settings);
        mSearchItem.setIcon(R.drawable.power_dialog_search);
        mVoiceItem.setIcon(R.drawable.power_dialog_voice);
        mLockItem.setIcon(R.drawable.power_dialog_lock);
        mBackItem.setIcon(R.drawable.power_dialog_back_arrow);

        setInitialAnimationState();
    }

    private void setInitialAnimationState() {
        AnimationSet outerLeftSet = new AnimationSet(true);
        outerLeftSet.setFillAfter(true);
        outerLeftSet.setDuration(1);
        outerLeftSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                animateEntrance();
            }
        });

        AnimationSet innerLeftSet = new AnimationSet(true);
        innerLeftSet.setFillAfter(true);
        innerLeftSet.setDuration(1);

        Animation innerLeft = new TranslateAnimation(0, -baseTranslationLarge * 2, 0, 0);
        innerLeftSet.addAnimation(innerLeft);

        AnimationSet innerRightSet = new AnimationSet(true);
        innerRightSet.setFillAfter(true);
        innerRightSet.setDuration(1);

        Animation innerRight = new TranslateAnimation(0, baseTranslationLarge * 2, 0, 0);
        innerRightSet.addAnimation(innerRight);

        Animation outerLeft = new TranslateAnimation(0, -baseTranslationLarge * 6, 0, 0);
        outerLeftSet.addAnimation(outerLeft);

        AnimationSet outerRightSet = new AnimationSet(true);
        outerRightSet.setFillAfter(true);
        outerRightSet.setDuration(1);

        Animation outerRight = new TranslateAnimation(0, baseTranslationLarge * 6, 0, 0);
        outerRightSet.addAnimation(outerRight);

        Animation alphaOut = new AlphaAnimation(1f, 0f);
        outerLeftSet.addAnimation(alphaOut);
        outerRightSet.addAnimation(alphaOut);
        innerLeftSet.addAnimation(alphaOut);
        innerRightSet.addAnimation(alphaOut);

        AnimationSet powerSet = new AnimationSet(true);
        powerSet.setFillAfter(true);
        powerSet.setDuration(1);

        AnimationSet rebootSet = new AnimationSet(true);
        rebootSet.setFillAfter(true);
        rebootSet.setDuration(1);

        Animation scaleDown = new ScaleAnimation(1f, 0f, 1f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        powerSet.addAnimation(scaleDown);
        rebootSet.addAnimation(scaleDown);

        Animation translateUp = new TranslateAnimation(0, 0, 0, baseTranslationLarge);
        rebootSet.addAnimation(translateUp);

        Animation translateDown = new TranslateAnimation(0, 0, 0, -baseTranslationLarge);
        powerSet.addAnimation(translateDown);

        mAirplaneItem.startAnimation(outerLeftSet);
        mSearchItem.startAnimation(outerLeftSet);
        mFlashlightItem.startAnimation(outerRightSet);
        mAudioItem.startAnimation(outerRightSet);
        mScreenshotItem.startAnimation(innerLeftSet);
        mVoiceItem.startAnimation(innerLeftSet);
        mSettingsItem.startAnimation(innerRightSet);
        mLockItem.startAnimation(innerRightSet);
        mPowerItem.startAnimation(powerSet);
        mRebootItem.startAnimation(rebootSet);
    }

    private void animateEntrance() {
        Interpolator decelerate = new DecelerateInterpolator();

        AnimationSet outerLeftSet = new AnimationSet(false);
        outerLeftSet.setFillAfter(true);
        outerLeftSet.setDuration(300);

        Animation outerLeft = new TranslateAnimation(baseTranslationLarge * 6, 0, 0, 0);
        outerLeft.setInterpolator(decelerate);
        outerLeftSet.addAnimation(outerLeft);

        AnimationSet outerRightSet = new AnimationSet(false);
        outerRightSet.setFillAfter(true);
        outerRightSet.setDuration(300);

        Animation outerRight = new TranslateAnimation(-baseTranslationLarge * 6, 0, 0, 0);
        outerRight.setInterpolator(decelerate);
        outerRightSet.addAnimation(outerRight);

        Animation outerAlphaIn = new AlphaAnimation(0f, 1f);
        outerAlphaIn.setDuration(300);
        outerLeftSet.addAnimation(outerAlphaIn);
        outerRightSet.addAnimation(outerAlphaIn);

        AnimationSet innerLeftSet = new AnimationSet(false);
        innerLeftSet.setDuration(300);
        innerLeftSet.setFillAfter(true);

        Animation innerLeft = new TranslateAnimation(baseTranslationLarge * 2, 0, 0, 0);
        innerLeft.setInterpolator(decelerate);
        innerLeftSet.addAnimation(innerLeft);

        AnimationSet innerRightSet = new AnimationSet(false);
        innerRightSet.setDuration(300);
        innerRightSet.setFillAfter(true);

        Animation innerRight = new TranslateAnimation(-baseTranslationLarge * 2, 0, 0, 0);
        innerRight.setInterpolator(decelerate);
        innerRightSet.addAnimation(innerRight);

        Animation innerAlphaIn = new AlphaAnimation(0f, 1f);
        innerLeftSet.addAnimation(innerAlphaIn);
        innerRightSet.addAnimation(innerAlphaIn);

        AnimationSet powerSet = new AnimationSet(false);
        powerSet.setFillAfter(true);
        powerSet.setDuration(500);

        AnimationSet rebootSet = new AnimationSet(false);
        rebootSet.setFillAfter(true);
        rebootSet.setDuration(500);

        Animation translateUp = new TranslateAnimation(0, 0, baseTranslationLarge, 0);
        translateUp.setInterpolator(decelerate);
        powerSet.addAnimation(translateUp);

        Animation translateDown = new TranslateAnimation(0, 0, -baseTranslationLarge, 0);
        translateDown.setInterpolator(decelerate);
        rebootSet.addAnimation(translateDown);

        Animation scaleUp = new ScaleAnimation(0.1f, 1f, 0.1f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleUp.setInterpolator(new OvershootInterpolator());
        powerSet.addAnimation(scaleUp);
        rebootSet.addAnimation(scaleUp);

        mAirplaneItem.startAnimation(outerLeftSet);
        mSearchItem.startAnimation(outerLeftSet);
        mFlashlightItem.startAnimation(outerRightSet);
        mAudioItem.startAnimation(outerRightSet);
        mScreenshotItem.startAnimation(innerLeftSet);
        mVoiceItem.startAnimation(innerLeftSet);
        mSettingsItem.startAnimation(innerRightSet);
        mLockItem.startAnimation(innerRightSet);
        mPowerItem.startAnimation(powerSet);
        mRebootItem.startAnimation(rebootSet);
    }

    private void animateRebootExpand() {
        Interpolator decelerate = new DecelerateInterpolator();

        AnimationSet rebootSet = new AnimationSet(false);
        rebootSet.setFillAfter(true);
        rebootSet.setDuration(300);

        Animation rebootTranslation = new TranslateAnimation(0, 0, buttonHeight + baseTranslationSmall * 2, 0);
        rebootTranslation.setInterpolator(decelerate);
        rebootSet.addAnimation(rebootTranslation);

        AnimationSet softSet = new AnimationSet(false);
        softSet.setFillAfter(true);
        softSet.setDuration(250);

        Animation softTranslation = new TranslateAnimation(0, 0, -baseTranslationSmall, 0);
        softTranslation.setInterpolator(decelerate);
        softSet.addAnimation(softTranslation);

        AnimationSet recoverySet = new AnimationSet(false);
        recoverySet.setFillAfter(true);
        recoverySet.setStartOffset(25);
        recoverySet.setDuration(250);

        Animation recoveryTranslation = new TranslateAnimation(0, 0, -baseTranslationSmall * 2, 0);
        recoveryTranslation.setInterpolator(decelerate);
        recoverySet.addAnimation(recoveryTranslation);

        AnimationSet bootloaderSet = new AnimationSet(false);
        bootloaderSet.setFillAfter(true);
        bootloaderSet.setStartOffset(50);
        bootloaderSet.setDuration(250);

        Animation bootloaderTranslation = new TranslateAnimation(0, 0, -baseTranslationSmall * 3, 0);
        bootloaderTranslation.setInterpolator(decelerate);
        bootloaderSet.addAnimation(bootloaderTranslation);

        AnimationSet uiSet = new AnimationSet(false);
        uiSet.setFillAfter(true);
        uiSet.setStartOffset(75);
        uiSet.setDuration(250);

        Animation uiTranslation = new TranslateAnimation(0, 0, -baseTranslationSmall * 4, 0);
        uiTranslation.setInterpolator(decelerate);
        uiSet.addAnimation(uiTranslation);

        AnimationSet backSet = new AnimationSet(false);
        backSet.setFillAfter(true);
        backSet.setDuration(250);

        Animation scaleIn = new ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleIn.setInterpolator(new OvershootInterpolator());
        backSet.addAnimation(scaleIn);

        Animation alphaIn = new AlphaAnimation(0f, 1f);
        softSet.addAnimation(alphaIn);
        recoverySet.addAnimation(alphaIn);
        bootloaderSet.addAnimation(alphaIn);
        uiSet.addAnimation(alphaIn);
        backSet.addAnimation(alphaIn);

        Animation alphaOut = new AlphaAnimation(1f, 0f);
        alphaOut.setDuration(250);
        alphaOut.setFillAfter(false);
        alphaOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                mPowerItem.setVisibility(View.GONE);
                mRebootItem.setVisibility(View.GONE);
            }
        });

        mPowerItem.startAnimation(alphaOut);
        mRebootItem.startAnimation(alphaOut);
        mRebootReboot.startAnimation(rebootSet);
        mRebootSoft.startAnimation(softSet);
        mRebootRecovery.startAnimation(recoverySet);
        mRebootBootloader.startAnimation(bootloaderSet);
        mRebootSystemUI.startAnimation(uiSet);
        mBackItem.startAnimation(backSet);

        mRebootReboot.setVisibility(View.VISIBLE);
        mRebootSoft.setVisibility(View.VISIBLE);
        mRebootRecovery.setVisibility(View.VISIBLE);
        mRebootBootloader.setVisibility(View.VISIBLE);
        mRebootSystemUI.setVisibility(View.VISIBLE);
        mBackItem.setVisibility(View.VISIBLE);
    }

    private void animateRebootCollapse() {
        Interpolator decelerate = new DecelerateInterpolator();
        Interpolator overshoot = new OvershootInterpolator();

        AnimationSet rebootSet = new AnimationSet(false);
        rebootSet.setFillAfter(false);
        rebootSet.setDuration(300);

        Animation rebootTranslation = new TranslateAnimation(0, 0, 0, (baseTranslationSmall * 5) + 10);
        rebootTranslation.setInterpolator(decelerate);
        rebootSet.addAnimation(rebootTranslation);

        AnimationSet softSet = new AnimationSet(false);
        softSet.setFillAfter(false);
        softSet.setStartOffset(100);
        softSet.setDuration(300);

        Animation softTranslation = new TranslateAnimation(0, 0, 0, baseTranslationSmall + 10);
        softTranslation.setInterpolator(decelerate);
        softSet.addAnimation(softTranslation);

        AnimationSet recoverySet = new AnimationSet(false);
        recoverySet.setFillAfter(false);
        recoverySet.setStartOffset(25);
        recoverySet.setDuration(250);

        AnimationSet bootloaderSet = new AnimationSet(false);
        bootloaderSet.setFillAfter(false);
        bootloaderSet.setStartOffset(100);
        bootloaderSet.setDuration(300);

        Animation bootloaderTranslation = new TranslateAnimation(0, 0, 0, -(baseTranslationSmall + 10));
        bootloaderTranslation.setInterpolator(decelerate);
        bootloaderSet.addAnimation(bootloaderTranslation);

        AnimationSet uiSet = new AnimationSet(false);
        uiSet.setFillAfter(false);
        uiSet.setDuration(300);

        Animation uiTranslation = new TranslateAnimation(0, 0, 0, -((baseTranslationSmall * 5) + 10));
        uiTranslation.setInterpolator(decelerate);
        uiSet.addAnimation(uiTranslation);

        AnimationSet backSet = new AnimationSet(false);
        backSet.setFillAfter(false);
        backSet.setDuration(300);

        Animation scaleOut = new ScaleAnimation(1f, 0f, 1f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleOut.setInterpolator(decelerate);
        backSet.addAnimation(scaleOut);
        recoverySet.addAnimation(scaleOut);

        Animation alphaOut = new AlphaAnimation(1f, 0f);
        rebootSet.addAnimation(alphaOut);
        softSet.addAnimation(alphaOut);
        recoverySet.addAnimation(alphaOut);
        bootloaderSet.addAnimation(alphaOut);
        uiSet.addAnimation(alphaOut);
        backSet.addAnimation(alphaOut);

        final AnimationSet powerSet = new AnimationSet(false);
        powerSet.setFillAfter(true);
        powerSet.setDuration(300);

        final AnimationSet mainRebootSet = new AnimationSet(false);
        mainRebootSet.setFillAfter(true);
        mainRebootSet.setDuration(300);
        mainRebootSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                mRebootReboot.setVisibility(View.GONE);
                mRebootSoft.setVisibility(View.GONE);
                mRebootRecovery.setVisibility(View.GONE);
                mRebootBootloader.setVisibility(View.GONE);
                mRebootSystemUI.setVisibility(View.GONE);
                mBackItem.setVisibility(View.GONE);
            }
        });

        Animation translateUp = new TranslateAnimation(0, 0, baseTranslationSmall * 4, 0);
        translateUp.setInterpolator(overshoot);
        mainRebootSet.addAnimation(translateUp);

        Animation translateDown = new TranslateAnimation(0, 0, -baseTranslationSmall * 4, 0);
        translateDown.setInterpolator(overshoot);
        powerSet.addAnimation(translateDown);

        mPowerItem.startAnimation(powerSet);
        mRebootItem.startAnimation(mainRebootSet);
        mRebootReboot.startAnimation(rebootSet);
        mRebootSoft.startAnimation(softSet);
        mRebootRecovery.startAnimation(recoverySet);
        mRebootBootloader.startAnimation(bootloaderSet);
        mRebootSystemUI.startAnimation(uiSet);
        mBackItem.startAnimation(backSet);

        mPowerItem.setVisibility(View.VISIBLE);
        mRebootItem.setVisibility(View.VISIBLE);
    }

    private void dismissDialog() {
        if (navBarHeight == 0) {
            navBarHeight = 48; // default nav bar height
        }
        Settings.System.putInt(mContext.getContentResolver(), Settings.System.NAVIGATION_BAR_HEIGHT, navBarHeight);
        dismiss();
    }

    private class ClickHandler implements View.OnClickListener {
        final PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);

        @Override
        public void onClick(View v) {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

            switch (v.getId()) {
                case R.id.power_dialog_power_item: // Turn off device
                    Log.d(TAG, "power button clicked");
                    IPowerManager ipm = IPowerManager.Stub.asInterface(ServiceManager.getService(Context.POWER_SERVICE));
                    try {
                        dismissDialog();
                        ipm.shutdown(false, false);
                    } catch (RemoteException e) {
                        Log.e(TAG, "Failure trying to power down!");
                    }
                    break;
                case R.id.power_dialog_reboot_item: // Show advanced reboot options
                    Log.d(TAG, "expand reboot items");
                    animateRebootExpand();
                    break;
                case R.id.power_dialog_reboot_reboot: // Reboot
                    Log.d(TAG, "reboot clicked");
                    dismissDialog();
                    pm.reboot(null);
                    break;
                case R.id.power_dialog_reboot_soft: // Perform soft reboot
                    Log.d(TAG, "reboot soft clicked");
                    // Make absolutely sure that we've restored the nav bar before continuing with soft reboot
                    if (navBarHeight != 0) {
                        Settings.System.putInt(mContext.getContentResolver(), Settings.System.NAVIGATION_BAR_HEIGHT, navBarHeight);
                    }
                    try {
                        if (Settings.System.getInt(mContext.getContentResolver(), Settings.System.NAVIGATION_BAR_HEIGHT) != 0) {
                            try { // Attempt to soft reboot the device
                                Process proc = Runtime.getRuntime().exec(new String[] { "su", "-c", "busybox killall system_server"});
                                proc.waitFor();
                            } catch (Exception ex) {
                                Log.d(TAG, "Unable to perform soft reboot", ex);
                            }
                        }
                    } catch (Settings.SettingNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                case R.id.power_dialog_reboot_recovery: // Reboot to recovery
                    Log.d(TAG, "reboot recovery clicked");
                    dismissDialog();
                    pm.reboot("recovery");
                    break;
                case R.id.power_dialog_reboot_bootloader: // Reboot to bootloader
                    Log.d(TAG, "reboot bootloader clicked");
                    dismissDialog();
                    pm.reboot("bootloader");
                    break;
                case R.id.power_dialog_reboot_systemui: // Restart SystemUI
                    Log.d(TAG, "reboot systemui clicked");
                    dismissDialog();
                    AllianceUtils.restartSystemUI();
                    break;
                case R.id.power_dialog_airplane_item: // Toggle airplane mode on/off
                    int newState;
                    if (AllianceUtils.isAirplaneModeOn(mContext)) {
                        mAirplaneItem.setIcon(R.drawable.power_dialog_airplane_off);
                        newState = 0;
                    } else {
                        mAirplaneItem.setIcon(R.drawable.power_dialog_airplane_on);
                        newState = 1;
                    }
                    Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, newState);
                    Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
                    intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
                    intent.putExtra("state", newState);
                    mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                    break;
                case R.id.power_dialog_screenshot_item: // Take screenshot
                    dismissDialog();
                    AllianceUtils.takeScreenshot(mContext);
                    break;
                case R.id.power_dialog_settings_item: // Launch Settings
                    Intent settingsIntent = mContext.getPackageManager().getLaunchIntentForPackage("com.android.settings");
                    settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    dismissDialog();
                    mContext.startActivity(settingsIntent);
                    break;
                case R.id.power_dialog_flashlight_item: // Toggle flashlight on/off
                    boolean state = mFlashlightController.isEnabled();
                    mFlashlightController.setFlashlight(!state);
                    mFlashlightItem.setIcon(state ? R.drawable.power_dialog_flashlight_off : R.drawable.power_dialog_flashlight_on);
                    break;
                case R.id.power_dialog_search_item: // Open Google search
                    Intent searchIntent = mContext.getPackageManager().getLaunchIntentForPackage("com.google.android.googlequicksearchbox");
                    searchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    dismissDialog();
                    mContext.startActivity(searchIntent);
                    break;
                case R.id.power_dialog_voice_item: // Start voice search
                    Intent voiceIntent = new Intent(Intent.ACTION_VOICE_ASSIST);
                    voiceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    dismissDialog();
                    mContext.startActivity(voiceIntent);
                    break;
                case R.id.power_dialog_lock_item: // lock the device
                    new LockPatternUtils(mContext).requireCredentialEntry(UserHandle.USER_ALL);
                    try {
                        dismissDialog();
                        WindowManagerGlobal.getWindowManagerService().lockNow(null);
                    } catch (RemoteException e) {
                        Log.e(TAG, "Error while attempting to lock device.", e);
                    }
                    break;
                case R.id.power_dialog_audio_item: // Change ringer mode
                    switch (AllianceUtils.getAudioMode(mContext)) {
                        case 0: // normal
                        default:
                            mAudioItem.setIcon(R.drawable.power_dialog_silent);
                            AllianceUtils.setAudioMode(mContext, 0); // set to silent
                            break;
                        case 1: // silent
                            mAudioItem.setIcon(R.drawable.power_dialog_vibrate);
                            AllianceUtils.setAudioMode(mContext, 1); // set to vibrate
                            break;
                        case 2: // vibrate
                            mAudioItem.setIcon(R.drawable.power_dialog_ring);
                            AllianceUtils.setAudioMode(mContext, 2); // set to normal
                            break;
                    }
                    break;
                case R.id.power_dialog_back:
                    animateRebootCollapse();
                    break;
                case R.id.power_dialog_root:
                    dismissDialog();
                    break;
            }
        }
    }
}
